package com.teleops.teleops_ai.ai.service;

import com.teleops.teleops_ai.ai.client.GroqClient;
import com.teleops.teleops_ai.ai.dto.*;
import com.teleops.teleops_ai.alarm.model.Alarm;
import com.teleops.teleops_ai.alarm.model.AlarmStatus;
import com.teleops.teleops_ai.alarm.model.RcaResult;
import com.teleops.teleops_ai.alarm.repository.AlarmRepository;
import com.teleops.teleops_ai.common.exception.BadRequestException;
import com.teleops.teleops_ai.common.exception.ResourceNotFoundException;
import com.teleops.teleops_ai.device.model.Device;
import com.teleops.teleops_ai.device.repository.DeviceRepository;
import com.teleops.teleops_ai.ticket.model.IncidentReport;
import com.teleops.teleops_ai.ticket.model.Ticket;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import com.teleops.teleops_ai.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Service
 *
 * Orchestrates all AI operations:
 *   1. Fetches relevant data from DB
 *   2. Builds a detailed, structured prompt
 *   3. Calls GroqClient with the prompt
 *   4. Parses the AI text response
 *   5. Saves results back to the DB
 *   6. Returns the DTO to the controller
 *
 * PROMPT ENGINEERING PRINCIPLE:
 *   The quality of AI output depends entirely on
 *   the quality of the prompt.
 *
 *   Good prompts:
 *     - Have a clear system role definition
 *     - Provide rich context (device type, vendor, severity)
 *     - Request a SPECIFIC output format
 *     - Tell the AI exactly what sections to include
 *
 *   Bad prompts:
 *     - "Analyze this alarm"
 *     - Vague output format requests
 *     - Missing context about the device or environment
 *
 *   We design each prompt carefully for telecom NOC context.
 *
 * PARSING STRATEGY:
 *   We ask the AI to respond in a LABELED format:
 *   "POSSIBLE_CAUSE: ..."
 *   "CONFIDENCE: ..."
 *   "SUGGESTED_FIX: ..."
 *
 *   Then we parse by splitting on these labels.
 *   This is more reliable than asking for JSON output
 *   from a language model (LLMs sometimes add extra text
 *   around JSON that breaks parsing).
 */
@Service
public class AiService {

    private static final Logger log =
            LoggerFactory.getLogger(AiService.class);

    private final GroqClient groqClient;
    private final AlarmRepository alarmRepository;
    private final DeviceRepository deviceRepository;
    private final TicketRepository ticketRepository;

    public AiService(GroqClient groqClient,
                     AlarmRepository alarmRepository,
                     DeviceRepository deviceRepository,
                     TicketRepository ticketRepository) {
        this.groqClient = groqClient;
        this.alarmRepository = alarmRepository;
        this.deviceRepository = deviceRepository;
        this.ticketRepository = ticketRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // Feature 1: Root Cause Analysis
    // ─────────────────────────────────────────────────────────────

    /**
     * Perform AI Root Cause Analysis on an alarm.
     *
     * Steps:
     *   1. Fetch alarm from DB
     *   2. Fetch linked device from DB
     *   3. Build rich contextual prompt
     *   4. Call Groq API
     *   5. Parse AI response into RcaResult
     *   6. Save RcaResult to alarm document
     *   7. Return RcaResponse to controller
     *
     * Business rules:
     *   - Alarm must exist
     *   - Alarm must not be RESOLVED (no point analyzing
     *     an already-resolved alarm)
     *   - Device linked to alarm must exist
     */
    public RcaResponse performRca(RcaRequest request) {

        // Step 1: Fetch alarm
        Alarm alarm = alarmRepository.findById(request.getAlarmId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alarm", "id", request.getAlarmId()));

        // Business rule: do not analyze resolved alarms
        if (alarm.getStatus() == AlarmStatus.RESOLVED) {
            throw new BadRequestException(
                    "Cannot perform RCA on a resolved alarm. " +
                            "The alarm has already been resolved.");
        }

        // Step 2: Fetch linked device
        Device device = deviceRepository
                .findById(alarm.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device linked to alarm", "id",
                        alarm.getDeviceId()));

        // Step 3: Build the prompt
        String systemPrompt = buildRcaSystemPrompt();
        String userMessage = buildRcaUserMessage(alarm, device);

        log.info("Performing RCA for alarm: {} on device: {}",
                alarm.getTitle(), device.getName());

        // Step 4: Call Groq API
        String aiResponse = groqClient.chat(
                systemPrompt, userMessage, 0.3);

        // Step 5: Parse response
        RcaResult rcaResult = parseRcaResponse(aiResponse,
                request.getAlarmId());

        // Step 6: Save to alarm document
        alarm.setRcaResult(rcaResult);
        alarmRepository.save(alarm);

        log.info("RCA completed for alarm: {}. " +
                        "Confidence: {}", alarm.getTitle(),
                rcaResult.getConfidence());

        // Step 7: Return DTO
        RcaResponse response = new RcaResponse(
                alarm.getId(),
                rcaResult.getPossibleCause(),
                rcaResult.getConfidence(),
                rcaResult.getSuggestedFix()
        );
        response.setAnalyzedAt(rcaResult.getAnalyzedAt());
        return response;
    }

    // ------------------------------------------------------------------------------//

    public RcaResponse performEnhancedRca(RcaRequest request) {

        // Step 1: Fetch current alarm
        Alarm alarm = alarmRepository.findById(request.getAlarmId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alarm", "id", request.getAlarmId()));

        if (alarm.getStatus() == AlarmStatus.RESOLVED) {
            throw new BadRequestException(
                    "Cannot perform RCA on a resolved alarm.");
        }

        // Step 2: Fetch device
        Device device = deviceRepository
                .findById(alarm.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", alarm.getDeviceId()));

        // Step 3: Fetch historical alarms on this device (last 10)
        List<Alarm> historicalAlarms = alarmRepository
                .findByDeviceId(device.getId())
                .stream()
                .filter(a -> !a.getId().equals(alarm.getId()))
                .limit(10)
                .collect(Collectors.toList());

        // Step 4: Fetch recent tickets on this device (last 5)
        List<Ticket> recentTickets = ticketRepository
                .findByDeviceId(device.getId())
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Step 5: Build enhanced prompt with context
        String systemPrompt = buildRcaSystemPrompt();
        String userMessage  = buildEnhancedRcaUserMessage(
                alarm, device, historicalAlarms, recentTickets);

        log.info("Enhanced RCA for alarm: {} | Historical context: " +
                        "{} alarms, {} tickets",
                alarm.getTitle(),
                historicalAlarms.size(),
                recentTickets.size());

        // Step 6: Call AI
        String aiResponse = groqClient.chat(systemPrompt, userMessage, 0.3);

        // Step 7: Parse and save result
        RcaResult rcaResult = parseRcaResponse(aiResponse, request.getAlarmId());
        alarm.setRcaResult(rcaResult);
        alarmRepository.save(alarm);

        RcaResponse response = new RcaResponse(
                alarm.getId(),
                rcaResult.getPossibleCause(),
                rcaResult.getConfidence(),
                rcaResult.getSuggestedFix()
        );
        response.setAnalyzedAt(rcaResult.getAnalyzedAt());
        return response;
    }

    // ----------------------------------------------------------------------------//


    // ─────────────────────────────────────────────────────────────
    // Feature 2: Log Summary
    // ─────────────────────────────────────────────────────────────

    /**
     * Summarize raw log content into plain English.
     *
     * This is a stateless operation.
     * No data is saved to the database.
     * Result is returned directly.
     */
    public LogSummaryResponse summarizeLogs(
            LogSummaryRequest request) {

        String systemPrompt = buildLogSummarySystemPrompt();
        String userMessage = buildLogSummaryUserMessage(request);

        log.info("Summarizing logs. Content length: {} chars",
                request.getLogContent().length());

        String aiResponse = groqClient.chat(
                systemPrompt, userMessage, 0.3);

        return parseLogSummaryResponse(aiResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // Feature 3: Incident Report Generation
    // ─────────────────────────────────────────────────────────────

    /**
     * Generate an AI incident report for a ticket.
     *
     * Steps:
     *   1. Fetch ticket from DB
     *   2. Fetch linked alarm if exists
     *   3. Build detailed incident context prompt
     *   4. Call Groq API
     *   5. Parse response into IncidentReport
     *   6. Save IncidentReport to ticket document
     *   7. Return IncidentReportResponse
     *
     * Business rules:
     *   - Ticket must be RESOLVED or CLOSED
     *   - Must have resolution documentation
     *   - Cannot generate for OPEN or IN_PROGRESS tickets
     */
    public IncidentReportResponse generateIncidentReport(
            IncidentReportRequest request) {

        // Fetch ticket
        Ticket ticket = ticketRepository
                .findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket", "id", request.getTicketId()));

        // Business rule: only for resolved/closed tickets
        if (ticket.getStatus() == TicketStatus.OPEN ||
                ticket.getStatus() == TicketStatus.IN_PROGRESS) {
            throw new BadRequestException(
                    "Incident report can only be generated for " +
                            "RESOLVED or CLOSED tickets. " +
                            "Current status: " + ticket.getStatus());
        }

        // Fetch linked alarm if exists
        Alarm linkedAlarm = null;
        if (ticket.getAlarmId() != null) {
            linkedAlarm = alarmRepository
                    .findById(ticket.getAlarmId())
                    .orElse(null);
        }

        // Build prompt
        String systemPrompt = buildIncidentReportSystemPrompt();
        String userMessage = buildIncidentReportUserMessage(
                ticket, linkedAlarm);

        log.info("Generating incident report for ticket: {}",
                ticket.getTicketNumber());

        // Call AI
        String aiResponse = groqClient.chat(
                systemPrompt, userMessage, 0.3);

        // Parse response
        IncidentReport report = parseIncidentReportResponse(
                aiResponse);

        // Save to ticket
        ticket.setIncidentReport(report);
        ticketRepository.save(ticket);

        log.info("Incident report generated for ticket: {}",
                ticket.getTicketNumber());

        // Build and return DTO
        IncidentReportResponse response = new IncidentReportResponse();
        response.setTicketId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setSummary(report.getSummary());
        response.setTimeline(report.getTimeline());
        response.setRootCause(report.getRootCause());
        response.setResolution(report.getResolution());
        response.setRecommendations(report.getRecommendations());
        response.setGeneratedAt(report.getGeneratedAt());
        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // Feature 4: AI Chat Assistant
    // ─────────────────────────────────────────────────────────────

    /**
     * Answer a free-form NOC engineering question.
     *
     * Stateless — no DB interaction.
     * Each question is independent.
     */
    public ChatResponse chat(ChatRequest request) {

        String systemPrompt = buildChatSystemPrompt();

        log.info("AI chat request: {}",
                request.getQuestion().substring(0,
                        Math.min(80, request.getQuestion().length())));

        String aiResponse = groqClient.chat(
                systemPrompt, request.getQuestion(), 0.5);

        return new ChatResponse(request.getQuestion(), aiResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // Prompt Builders
    // ─────────────────────────────────────────────────────────────

    /**
     * System prompt for RCA.
     *
     * Defines the AI's role as a telecom expert.
     * Instructs it to respond in a specific labeled format.
     *
     * The labeled format is critical for reliable parsing.
     * We tell the AI EXACTLY what labels to use.
     */
    private String buildRcaSystemPrompt() {
        return """
                You are a senior telecom network engineer with 15 years
                of experience in NOC operations. You specialize in
                analyzing network alarms and identifying root causes
                for 4G/5G towers, routers, firewalls, switches, and
                base stations.

                When given an alarm, you must analyze it and respond
                in EXACTLY this format with these exact labels:

                POSSIBLE_CAUSE: [One clear sentence describing the most
                likely root cause based on the alarm details and device type]

                CONFIDENCE: [HIGH, MEDIUM, or LOW based on how certain
                you are given the available information]

                SUGGESTED_FIX: [2-4 specific actionable steps an NOC
                engineer should take to resolve this issue]

                Do not include any text before POSSIBLE_CAUSE or after
                SUGGESTED_FIX. Respond only in the format above.
                """;
    }

    /**
     * User message for RCA — rich context about the alarm and device.
     *
     * The more context we provide, the better the AI analysis.
     * We include: alarm details, severity, device type,
     * vendor, location, and current device status.
     */
    private String buildRcaUserMessage(Alarm alarm, Device device) {
        return String.format("""
                Please analyze this network alarm and identify the root cause.

                ALARM DETAILS:
                Title: %s
                Description: %s
                Severity: %s
                Status: %s
                Raised At: %s

                AFFECTED DEVICE:
                Name: %s
                Type: %s
                Vendor: %s
                Model: %s
                Location: %s
                Current Status: %s
                IP Address: %s
                """,
                alarm.getTitle(),
                alarm.getDescription(),
                alarm.getSeverity(),
                alarm.getStatus(),
                alarm.getRaisedAt() != null
                        ? alarm.getRaisedAt().format(
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "Unknown",
                device.getName(),
                device.getType(),
                device.getVendor() != null ? device.getVendor() : "Unknown",
                device.getModel() != null ? device.getModel() : "Unknown",
                device.getLocation(),
                device.getStatus(),
                device.getIpAddress()
        );
    }

    // ------------------------------------------------------------------------------ //

    private String buildEnhancedRcaUserMessage(Alarm alarm,
                                               Device device,
                                               List<Alarm> history,
                                               List<Ticket> tickets) {
        StringBuilder sb = new StringBuilder();

        // Current alarm
        sb.append("CURRENT ALARM TO ANALYZE:\n");
        sb.append("Title: ").append(alarm.getTitle()).append("\n");
        sb.append("Description: ").append(alarm.getDescription()).append("\n");
        sb.append("Severity: ").append(alarm.getSeverity()).append("\n\n");

        // Device info
        sb.append("AFFECTED DEVICE:\n");
        sb.append("Name: ").append(device.getName()).append("\n");
        sb.append("Type: ").append(device.getType()).append("\n");
        sb.append("Vendor: ").append(device.getVendor() != null
                ? device.getVendor() : "Unknown").append("\n");
        sb.append("Location: ").append(device.getLocation()).append("\n");
        sb.append("Current Status: ").append(device.getStatus()).append("\n\n");

        // Historical alarms context
        if (!history.isEmpty()) {
            sb.append("HISTORICAL ALARMS ON THIS DEVICE (last ")
                    .append(history.size()).append("):\n");
            history.forEach(h -> {
                sb.append("- [").append(h.getSeverity()).append("] ")
                        .append(h.getTitle())
                        .append(" | Status: ").append(h.getStatus());
                if (h.getStatus() == AlarmStatus.RESOLVED &&
                        h.getDescription() != null &&
                        h.getDescription().contains("[RESOLUTION]")) {
                    // Extract resolution note
                    int idx = h.getDescription().indexOf("[RESOLUTION]:");
                    if (idx >= 0) {
                        String resolution = h.getDescription()
                                .substring(idx + 14).trim();
                        sb.append(" | Resolution: ")
                                .append(resolution, 0, Math.min(100,
                                        resolution.length()));
                    }
                }
                sb.append("\n");
            });
            sb.append("\n");
        }

        // Recent tickets context
        if (!tickets.isEmpty()) {
            sb.append("RECENT TICKETS FOR THIS DEVICE (last ")
                    .append(tickets.size()).append("):\n");
            tickets.forEach(t -> {
                sb.append("- [").append(t.getPriority()).append("] ")
                        .append(t.getTitle())
                        .append(" | Status: ").append(t.getStatus());
                if (t.getResolution() != null) {
                    sb.append(" | Resolution: ")
                            .append(t.getResolution(), 0,
                                    Math.min(100, t.getResolution().length()));
                }
                sb.append("\n");
            });
            sb.append("\n");
        }

        sb.append("Based on the current alarm AND the historical context above, ")
                .append("provide your root cause analysis.");

        return sb.toString();
    }

    // ----------------------------------------------------------------------------- //

    /**
     * System prompt for log summarization.
     */
    private String buildLogSummarySystemPrompt() {
        return """
                You are a telecom network engineer expert at reading
                and interpreting device logs, system logs, and network
                event logs. You can quickly identify important events,
                errors, and patterns in raw log data.

                When given log content, respond in EXACTLY this format:

                SUMMARY: [2-3 sentences explaining what is happening
                in plain English]

                KEY_EVENTS: [List the 3-5 most important events or
                errors found in the logs, one per line starting with
                a dash]

                SEVERITY: [CRITICAL, HIGH, MEDIUM, LOW, or INFO based
                on what you found in the logs]

                Do not include any text outside this format.
                """;
    }

    /**
     * User message for log summarization.
     */
    private String buildLogSummaryUserMessage(
            LogSummaryRequest request) {

        StringBuilder sb = new StringBuilder();

        if (request.getContext() != null &&
                !request.getContext().isBlank()) {
            sb.append("Context: ").append(request.getContext())
                    .append("\n\n");
        }

        sb.append("Log Content:\n")
                .append(request.getLogContent());

        return sb.toString();
    }

    /**
     * System prompt for incident report.
     */
    private String buildIncidentReportSystemPrompt() {
        return """
                You are a professional telecom NOC documentation
                specialist. You write clear, structured incident
                reports for management review.

                When given incident details, respond in EXACTLY
                this format:

                SUMMARY: [2-3 sentence executive summary of the
                incident suitable for management. Include what happened,
                when, and the impact.]

                TIMELINE: [Chronological list of key events from
                ticket creation to resolution, one event per line
                starting with a dash and timestamp if available]

                ROOT_CAUSE: [Clear technical explanation of why
                the incident occurred]

                RESOLUTION: [What was done to fix the issue,
                in 3-5 clear steps]

                RECOMMENDATIONS: [2-4 recommendations to prevent
                this incident from recurring]

                Do not include any text outside this format.
                """;
    }

    /**
     * User message for incident report — full ticket context.
     */
    private String buildIncidentReportUserMessage(
            Ticket ticket, Alarm linkedAlarm) {

        StringBuilder sb = new StringBuilder();
        sb.append("Please generate an incident report for:\n\n");
        sb.append("TICKET INFORMATION:\n");
        sb.append("Ticket Number: ").append(ticket.getTicketNumber())
                .append("\n");
        sb.append("Title: ").append(ticket.getTitle()).append("\n");
        sb.append("Description: ").append(ticket.getDescription())
                .append("\n");
        sb.append("Priority: ").append(ticket.getPriority()).append("\n");
        sb.append("Status: ").append(ticket.getStatus()).append("\n");
        sb.append("Device: ").append(ticket.getDeviceName()).append("\n");
        sb.append("Assigned To: ").append(
                        ticket.getAssignedName() != null
                                ? ticket.getAssignedName() : "Unassigned")
                .append("\n");

        if (ticket.getCreatedAt() != null) {
            sb.append("Created At: ")
                    .append(ticket.getCreatedAt().format(
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .append("\n");
        }
        if (ticket.getResolvedAt() != null) {
            sb.append("Resolved At: ")
                    .append(ticket.getResolvedAt().format(
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .append("\n");
        }

        if (ticket.getResolution() != null) {
            sb.append("Resolution Notes: ")
                    .append(ticket.getResolution()).append("\n");
        }

        if (linkedAlarm != null) {
            sb.append("\nLINKED ALARM:\n");
            sb.append("Alarm Title: ").append(linkedAlarm.getTitle())
                    .append("\n");
            sb.append("Severity: ").append(linkedAlarm.getSeverity())
                    .append("\n");
            sb.append("Description: ")
                    .append(linkedAlarm.getDescription()).append("\n");

            if (linkedAlarm.getRcaResult() != null) {
                sb.append("RCA Result: ")
                        .append(linkedAlarm.getRcaResult().getPossibleCause())
                        .append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * System prompt for AI chat assistant.
     */
    private String buildChatSystemPrompt() {
        return """
                You are an expert NOC (Network Operations Center)
                assistant for a telecom company. You help engineers
                with questions about:
                - 4G/5G network equipment and troubleshooting
                - Router, firewall, and switch configuration
                - Network alarm analysis and resolution
                - Telecom standards and protocols
                - Best practices for NOC operations

                Give concise, practical answers. Focus on actionable
                information. Use technical terminology appropriate for
                experienced network engineers.

                If a question is outside telecom/networking scope,
                politely redirect the engineer to focus on NOC-related
                questions.
                """;
    }

    // ─────────────────────────────────────────────────────────────
    // Response Parsers
    // ─────────────────────────────────────────────────────────────

    /**
     * Parse AI RCA response into RcaResult.
     *
     * Expected format:
     *   POSSIBLE_CAUSE: ...
     *   CONFIDENCE: ...
     *   SUGGESTED_FIX: ...
     *
     * Parsing strategy:
     *   Split on the label markers and extract values.
     *   If parsing fails (AI went off-format), use the raw
     *   response as possibleCause to avoid losing the data.
     */
    private RcaResult parseRcaResponse(String aiResponse,
                                       String alarmId) {
        RcaResult result = new RcaResult();
        result.setAnalyzedAt(LocalDateTime.now());

        try {
            result.setPossibleCause(
                    extractLabel(aiResponse, "POSSIBLE_CAUSE:"));
            result.setConfidence(
                    extractLabel(aiResponse, "CONFIDENCE:"));
            result.setSuggestedFix(
                    extractLabel(aiResponse, "SUGGESTED_FIX:"));

        } catch (Exception e) {
            log.warn("Could not parse structured RCA response " +
                    "for alarm {}. Using raw response.", alarmId);
            result.setPossibleCause(aiResponse);
            result.setConfidence("MEDIUM");
            result.setSuggestedFix(
                    "Please review the AI analysis above and " +
                            "apply appropriate remediation steps.");
        }

        return result;
    }

    /**
     * Parse AI log summary response into LogSummaryResponse.
     */
    private LogSummaryResponse parseLogSummaryResponse(
            String aiResponse) {

        LogSummaryResponse response = new LogSummaryResponse();
        response.setAnalyzedAt(LocalDateTime.now());

        try {
            response.setSummary(
                    extractLabel(aiResponse, "SUMMARY:"));
            response.setKeyEvents(
                    extractLabel(aiResponse, "KEY_EVENTS:"));
            response.setSeverity(
                    extractLabel(aiResponse, "SEVERITY:"));

        } catch (Exception e) {
            log.warn("Could not parse structured log summary. " +
                    "Using raw response.");
            response.setSummary(aiResponse);
            response.setKeyEvents("See summary above.");
            response.setSeverity("UNKNOWN");
        }

        return response;
    }

    /**
     * Parse AI incident report response into IncidentReport.
     */
    private IncidentReport parseIncidentReportResponse(
            String aiResponse) {

        IncidentReport report = new IncidentReport();
        report.setGeneratedAt(LocalDateTime.now());

        try {
            report.setSummary(
                    extractLabel(aiResponse, "SUMMARY:"));
            report.setTimeline(
                    extractLabel(aiResponse, "TIMELINE:"));
            report.setRootCause(
                    extractLabel(aiResponse, "ROOT_CAUSE:"));
            report.setResolution(
                    extractLabel(aiResponse, "RESOLUTION:"));
            report.setRecommendations(
                    extractLabel(aiResponse, "RECOMMENDATIONS:"));

        } catch (Exception e) {
            log.warn("Could not parse structured incident report. " +
                    "Using raw response.");
            report.setSummary(aiResponse);
            report.setTimeline("See summary above.");
            report.setRootCause("See summary above.");
            report.setResolution("See summary above.");
            report.setRecommendations(
                    "Review AI analysis and implement appropriate " +
                            "preventive measures.");
        }

        return report;
    }

    /**
     * Extract the value after a labeled section header.
     *
     * Given: "POSSIBLE_CAUSE: Power failure at site\nCONFIDENCE: HIGH"
     * extractLabel(text, "POSSIBLE_CAUSE:") → "Power failure at site"
     *
     * Strategy:
     *   1. Find the index of the label
     *   2. Extract text from after label to next label or end
     *   3. Trim whitespace
     */
    private String extractLabel(String text, String label) {
        int startIdx = text.indexOf(label);
        if (startIdx == -1) {
            return "Not available";
        }

        // Start after the label
        int contentStart = startIdx + label.length();

        // Find the next label (next section starts with all-caps word
        // followed by colon)
        String[] nextLabels = {
                "POSSIBLE_CAUSE:", "CONFIDENCE:", "SUGGESTED_FIX:",
                "SUMMARY:", "KEY_EVENTS:", "SEVERITY:",
                "TIMELINE:", "ROOT_CAUSE:", "RESOLUTION:",
                "RECOMMENDATIONS:"
        };

        int contentEnd = text.length();
        for (String nextLabel : nextLabels) {
            if (nextLabel.equals(label)) continue;
            int nextIdx = text.indexOf(nextLabel, contentStart);
            if (nextIdx != -1 && nextIdx < contentEnd) {
                contentEnd = nextIdx;
            }
        }

        return text.substring(contentStart, contentEnd).trim();
    }
}



