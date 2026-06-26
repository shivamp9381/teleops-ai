package com.teleops.teleops_ai.ai.controller;

import com.teleops.teleops_ai.ai.dto.*;
import com.teleops.teleops_ai.ai.service.AiService;
import com.teleops.teleops_ai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI Controller
 *
 * REST endpoints for all AI-powered features.
 *
 * Access control:
 *   POST /ai/rca              = NOC_ENGINEER+
 *   POST /ai/log-summary      = NOC_ENGINEER+
 *   POST /ai/incident-report  = NOC_MANAGER+
 *   POST /ai/chat             = NOC_ENGINEER+
 *
 * Why restrict incident-report to MANAGER+?
 *   Incident reports are formal documents for management.
 *   They consume more API tokens (cost).
 *   Engineers have RCA which is their operational tool.
 *   Managers generate the final formal report.
 *
 * Important note on latency:
 *   AI endpoints take 1-5 seconds to respond.
 *   This is normal - we are waiting for the Groq API.
 *   The frontend should show a loading spinner.
 *   Do NOT add timeouts shorter than 30 seconds.
 */
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Operations",
        description = "AI-powered Root Cause Analysis, Log Summarization, " +
                "Incident Report generation, and NOC Chat Assistant")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * POST /api/v1/ai/rca
     *
     * Perform Root Cause Analysis on an alarm.
     * Saves the result to the alarm document in MongoDB.
     * Returns the analysis immediately.
     *
     * Expected response time: 1-3 seconds.
     */
    @PostMapping("/rca")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Root Cause Analysis",
            description = "Analyze an alarm using AI to identify the root cause " +
                    "and get a suggested fix. Result is saved to the alarm."
    )
    public ResponseEntity<ApiResponse<RcaResponse>> performRca(
            @Valid @RequestBody RcaRequest request) {

        RcaResponse response = aiService.performRca(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Root cause analysis completed.", response));
    }

    /**
     * POST /api/v1/ai/log-summary
     *
     * Summarize raw log content into plain English.
     * Result is NOT saved to database.
     *
     * Expected response time: 1-3 seconds.
     */
    @PostMapping("/log-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Summarize logs",
            description = "Paste raw device or system logs and get a plain " +
                    "English summary of what happened."
    )
    public ResponseEntity<ApiResponse<LogSummaryResponse>> summarizeLogs(
            @Valid @RequestBody LogSummaryRequest request) {

        LogSummaryResponse response = aiService.summarizeLogs(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Log summary generated.", response));
    }

    /**
     * POST /api/v1/ai/incident-report
     *
     * Generate a formal incident report for a ticket.
     * Saves the report to the ticket document in MongoDB.
     * Restricted to managers.
     *
     * Expected response time: 2-5 seconds.
     */
    @PostMapping("/incident-report")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Generate incident report",
            description = "Generate a formal 5-section incident report for a " +
                    "resolved or closed ticket. Report is saved to the ticket."
    )
    public ResponseEntity<ApiResponse<IncidentReportResponse>>
    generateIncidentReport(
            @Valid @RequestBody IncidentReportRequest request) {

        IncidentReportResponse response =
                aiService.generateIncidentReport(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Incident report generated successfully.",
                        response));
    }

    /**
     * POST /api/v1/ai/chat
     *
     * Free-form AI chat for NOC engineers.
     * Ask any telecom/networking question.
     * Result is NOT saved to database.
     *
     * Expected response time: 1-3 seconds.
     */
    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "AI NOC Chat Assistant",
            description = "Ask the AI assistant any telecom or NOC operations " +
                    "question. Get expert answers instantly."
    )
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {

        ChatResponse response = aiService.chat(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "AI response generated.", response));
    }

    @PostMapping("/rca/enhanced")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Enhanced Root Cause Analysis",
            description = "RCA with historical alarm and ticket context. " +
                    "Provides significantly better results."
    )
    public ResponseEntity<ApiResponse<RcaResponse>> performEnhancedRca(
            @Valid @RequestBody RcaRequest request) {

        RcaResponse response = aiService.performEnhancedRca(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Enhanced root cause analysis completed.", response));
    }
}