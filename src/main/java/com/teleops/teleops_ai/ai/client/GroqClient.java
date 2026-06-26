package com.teleops.teleops_ai.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Groq API Client
 *
 * Low-level HTTP client for the Groq AI inference API.
 *
 * Responsibilities:
 *   1. Build the HTTP request in Groq's expected format
 *   2. Send the request with proper auth headers
 *   3. Parse the response and extract the AI text
 *   4. Handle errors gracefully
 *
 * This class knows NOTHING about our business logic.
 * It only knows how to talk to Groq API.
 *
 * The AiService is responsible for:
 *   - Building the right prompt
 *   - Calling this client
 *   - Parsing the AI text response into structured objects
 *   - Saving results to the database
 *
 * Groq API Request Format:
 * {
 *   "model": "llama3-8b-8192",
 *   "messages": [
 *     { "role": "system", "content": "You are a telecom expert..." },
 *     { "role": "user", "content": "Analyze this alarm..." }
 *   ],
 *   "max_tokens": 1024,
 *   "temperature": 0.3
 * }
 *
 * Groq API Response Format:
 * {
 *   "choices": [
 *     {
 *       "message": {
 *         "content": "AI response text here"
 *       }
 *     }
 *   ]
 * }
 *
 * Temperature setting:
 *   0.0 = fully deterministic (same prompt = same answer)
 *   1.0 = very creative/random
 *   0.3 = mostly deterministic, slight variation
 *   We use 0.3 for technical analysis (we want consistent answers).
 *   We use 0.5 for chat (slightly more conversational).
 */
@Component
public class GroqClient {

    private static final Logger log =
            LoggerFactory.getLogger(GroqClient.class);

    @Value("${application.groq.api-key}")
    private String apiKey;

    @Value("${application.groq.base-url}")
    private String baseUrl;

    @Value("${application.groq.model}")
    private String model;

    @Value("${application.groq.max-tokens}")
    private int maxTokens;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GroqClient(RestTemplate restTemplate,
                      ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ─────────────────────────────────────────────────────────────
    // Main Chat Completion Method
    // ─────────────────────────────────────────────────────────────

    /**
     * Send a chat completion request to Groq API.
     *
     * @param systemPrompt  Instructions that define the AI's role
     *                      and how it should respond.
     *                      Example: "You are a telecom expert NOC analyst."
     *
     * @param userMessage   The actual question or content to analyze.
     *                      Example: "Alarm: Tower offline. Device: 5G tower..."
     *
     * @param temperature   Controls response randomness (0.0 - 1.0).
     *                      Use 0.3 for technical analysis.
     *                      Use 0.5 for conversational chat.
     *
     * @return              Raw text response from the AI model.
     *
     * @throws RuntimeException if the API call fails.
     */
    public String chat(String systemPrompt, String userMessage,
                       double temperature) {

        try {
            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Build request body as JSON
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);

            // Build messages array
            ArrayNode messages = objectMapper.createArrayNode();

            // System message defines AI persona and behavior
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            // User message is the actual input
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.set("messages", messages);

            // Send the request
            HttpEntity<String> request = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            log.debug("Sending request to Groq API: model={}, " +
                    "tokens={}", model, maxTokens);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions",
                    request,
                    String.class
            );

            // Parse and extract the response text
            String responseText = extractContent(
                    response.getBody());

            log.debug("Groq API responded successfully. " +
                            "Response length: {} chars",
                    responseText.length());

            return responseText;

        } catch (HttpClientErrorException e) {
            // 4xx errors: bad request, auth failure, rate limit
            log.error("Groq API client error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "AI service request failed: " + e.getMessage());

        } catch (HttpServerErrorException e) {
            // 5xx errors: Groq server issues
            log.error("Groq API server error: {}", e.getMessage());
            throw new RuntimeException(
                    "AI service is temporarily unavailable. " +
                            "Please try again.");

        } catch (ResourceAccessException e) {
            // Network timeout or connection refused
            log.error("Groq API connection failed: {}", e.getMessage());
            throw new RuntimeException(
                    "Could not connect to AI service. " +
                            "Please check network connectivity.");

        } catch (Exception e) {
            log.error("Unexpected error calling Groq API: {}",
                    e.getMessage(), e);
            throw new RuntimeException(
                    "AI analysis failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Extract the text content from Groq's JSON response.
     *
     * Response structure:
     * {
     *   "choices": [
     *     {
     *       "message": {
     *         "content": "The actual AI response text"
     *       }
     *     }
     *   ]
     * }
     *
     * We navigate: choices[0].message.content
     */
    private String extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            log.error("Failed to parse Groq API response: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Failed to parse AI response.");
        }
    }
}