package com.teleops.teleops_ai.ai.dto;

import java.time.LocalDateTime;

/**
 * Chat Response DTO
 *
 * Simple response wrapper for AI chat answers.
 * Not persisted to database.
 */
public class ChatResponse {

    private String question;
    private String answer;
    private LocalDateTime answeredAt;

    // ─────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────

    public ChatResponse() {
    }

    public ChatResponse(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.answeredAt = LocalDateTime.now();
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}