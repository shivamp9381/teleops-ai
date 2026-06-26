package com.teleops.teleops_ai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Log Summary Request DTO
 *
 * The engineer pastes raw log text into this field.
 * The AI returns a plain English summary.
 *
 * Logs can be long. We set a generous max size
 * but cap it to prevent extremely large requests
 * that would exhaust the Groq API context window.
 *
 * 8000 characters ≈ ~2000 tokens.
 * Llama3-8b-8192 has an 8192 token context window.
 * We reserve ~6000 tokens for the system prompt +
 * response, leaving ~2000 for the log input.
 */
public class LogSummaryRequest {

    @NotBlank(message = "Log content is required")
    @Size(min = 50, max = 8000,
            message = "Log content must be between 50 and 8000 characters")
    private String logContent;

    /**
     * Optional context: what system generated these logs?
     * Example: "nginx access log", "kernel syslog",
     *          "Ericsson RBS alarm log"
     * Helps AI give more relevant summaries.
     */
    private String context;

    // ─────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────

    public LogSummaryRequest() {
    }

    public LogSummaryRequest(String logContent, String context) {
        this.logContent = logContent;
        this.context = context;
    }

    // ─────────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────────

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}