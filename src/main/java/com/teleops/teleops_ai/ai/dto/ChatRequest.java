package com.teleops.teleops_ai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Chat Request DTO
 *
 * Free-form AI chat for NOC engineers.
 * Engineers can ask questions like:
 *   "What does error code 0x80004005 mean on an Ericsson RBS?"
 *   "How do I reset a Nokia base station?"
 *   "What causes intermittent signal loss on 5G towers?"
 *
 * This is stateless — each request is independent.
 * We do not maintain conversation history.
 * Each question is answered in isolation.
 *
 * For a production system, you would add a sessionId
 * and maintain conversation history per session.
 * That is Phase 18 enhancement territory.
 */
public class ChatRequest {

    @NotBlank(message = "Question is required")
    @Size(min = 5, max = 2000,
            message = "Question must be between 5 and 2000 characters")
    private String question;

    // ─────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────

    public ChatRequest() {
    }

    public ChatRequest(String question) {
        this.question = question;
    }

    // ─────────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────────

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}