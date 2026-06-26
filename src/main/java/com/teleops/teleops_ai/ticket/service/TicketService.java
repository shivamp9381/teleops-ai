package com.teleops.teleops_ai.ticket.service;

import com.teleops.teleops_ai.audit.model.AuditAction;
import com.teleops.teleops_ai.audit.repository.AuditRepository;
import com.teleops.teleops_ai.audit.service.AuditService;
import com.teleops.teleops_ai.auth.model.User;
import com.teleops.teleops_ai.auth.repository.UserRepository;
import com.teleops.teleops_ai.common.exception.BadRequestException;
import com.teleops.teleops_ai.common.exception.ResourceNotFoundException;
import com.teleops.teleops_ai.device.model.Device;
import com.teleops.teleops_ai.device.repository.DeviceRepository;
import com.teleops.teleops_ai.ticket.dto.AssignTicketRequest;
import com.teleops.teleops_ai.ticket.dto.ResolveTicketRequest;
import com.teleops.teleops_ai.ticket.dto.TicketRequest;
import com.teleops.teleops_ai.ticket.dto.TicketResponse;
import com.teleops.teleops_ai.ticket.model.Ticket;
import com.teleops.teleops_ai.ticket.model.TicketPriority;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import com.teleops.teleops_ai.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ticket Service
 *
 * Business logic for the complete ticket lifecycle.
 *
 * Cross-module dependencies:
 *   - DeviceRepository: validate device exists, get device name
 *   - UserRepository:   validate assignee exists, get assignee name
 *
 * Business rules enforced:
 *   1. Device must exist when creating a ticket
 *   2. New tickets always start as OPEN
 *   3. Ticket number is auto-generated, never client-provided
 *   4. Assignee must be a valid user
 *   5. Status transitions are strictly enforced
 *   6. CLOSED tickets cannot be modified
 *   7. Resolution is mandatory before closing
 *   8. resolvedAt and closedAt are set automatically
 *   9. createdBy is from SecurityContext, not request body
 *
 * Ticket number generation:
 *   Format: TKT-YYYY-NNNNN
 *   We count total tickets and increment.
 *   Thread safety note:
 *     In a high-concurrency system, this could produce
 *     duplicates. MongoDB's unique index on ticketNumber
 *     prevents actual duplicates from being saved.
 *     For this scale, count-based generation is sufficient.
 */
@Service
public class TicketService {

    private static final Logger log =
            LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public TicketService(TicketRepository ticketRepository,
                         DeviceRepository deviceRepository,
                         UserRepository userRepository, AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    // ─────────────────────────────────────────
    // Create Ticket
    // ─────────────────────────────────────────

    /**
     * Create a new incident ticket.
     *
     * Steps:
     *   1. Validate device exists
     *   2. Get current user from SecurityContext
     *   3. Generate ticket number
     *   4. Create ticket with OPEN status
     *   5. Save and return
     *
     * Ticket starts as OPEN and unassigned.
     * A manager must assign it to an engineer.
     */
    public TicketResponse createTicket(TicketRequest request) {

        // Step 1: Validate device exists
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", request.getDeviceId()));

        // Step 2: Get current user from SecurityContext
        String currentUserEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Step 3: Generate ticket number
        String ticketNumber = generateTicketNumber();

        // Step 4: Create ticket
        Ticket ticket = new Ticket(
                ticketNumber,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                device.getId(),
                device.getName(),
                currentUserEmail,
                request.getAlarmId()
        );

        Ticket savedTicket = ticketRepository.save(ticket);

        auditService.log(currentUserEmail,
                AuditAction.TICKET_CREATED, "Ticket",
                savedTicket.getId(), savedTicket.getTicketNumber(),
                "Priority: " + savedTicket.getPriority());

        log.info("Ticket created: {} [{}] for device {} by {}",
                savedTicket.getTicketNumber(),
                savedTicket.getPriority(),
                device.getName(),
                currentUserEmail);

        return TicketResponse.fromTicket(savedTicket);
    }

    // ─────────────────────────────────────────
    // Get All Tickets
    // ─────────────────────────────────────────

    /**
     * Get all tickets, most recent first.
     */
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Get Ticket By ID
    // ─────────────────────────────────────────

    /**
     * Get a single ticket by MongoDB ID.
     */
    public TicketResponse getTicketById(String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket", "id", id));

        return TicketResponse.fromTicket(ticket);
    }

    // ─────────────────────────────────────────
    // Get Ticket By Number
    // ─────────────────────────────────────────

    /**
     * Get a ticket by its human-readable ticket number.
     * Engineers use this to look up tickets by reference.
     */
    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository
                .findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket", "ticketNumber", ticketNumber));

        return TicketResponse.fromTicket(ticket);
    }

    // ─────────────────────────────────────────
    // Get Tickets By Status
    // ─────────────────────────────────────────

    /**
     * Get all tickets with a specific status.
     */
    public List<TicketResponse> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status)
                .stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Get My Tickets
    // ─────────────────────────────────────────

    /**
     * Get all tickets assigned to the current authenticated engineer.
     * Engineers use this for their personal work queue.
     */
    public List<TicketResponse> getMyTickets() {
        String currentUserEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ticketRepository.findByAssignedTo(currentUserEmail)
                .stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Get Tickets By Device
    // ─────────────────────────────────────────

    /**
     * Get all tickets for a specific device.
     * Used in device detail view for ticket history.
     */
    public List<TicketResponse> getTicketsByDevice(String deviceId) {

        deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", deviceId));

        return ticketRepository.findByDeviceId(deviceId)
                .stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Assign Ticket
    // ─────────────────────────────────────────

    /**
     * Assign a ticket to an engineer.
     *
     * Business rules:
     *   - Only OPEN tickets can be assigned initially
     *   - CLOSED tickets cannot be assigned
     *   - The assignee must be a valid user
     *
     * Assignment moves ticket from OPEN to IN_PROGRESS.
     *
     * We look up the assignee to get their name
     * for denormalization in the ticket document.
     */
    public TicketResponse assignTicket(String id,
                                       AssignTicketRequest request) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket", "id", id));

        // Enforce: cannot assign a closed ticket
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException(
                    "Cannot assign a closed ticket.");
        }

        // Enforce: cannot assign an already resolved ticket
        if (ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new BadRequestException(
                    "Cannot assign a resolved ticket.");
        }

        // Validate assignee exists
        User assignee = userRepository.findById(
                        request.getAssignedToUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.getAssignedToUserId()));

        ticket.setAssignedTo(assignee.getEmail());
        ticket.setAssignedName(assignee.getName());
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        Ticket updatedTicket = ticketRepository.save(ticket);

        log.info("Ticket {} assigned to {}",
                updatedTicket.getTicketNumber(),
                assignee.getName());

        auditService.log(
                SecurityContextHolder.getContext()
                        .getAuthentication().getName(),
                AuditAction.TICKET_ASSIGNED, "Ticket",
                updatedTicket.getId(), updatedTicket.getTicketNumber(),
                "Assigned to: " + assignee.getName());

        return TicketResponse.fromTicket(updatedTicket);
    }

    // ─────────────────────────────────────────
    // Resolve Ticket
    // ─────────────────────────────────────────

    /**
     * Resolve a ticket with documentation.
     *
     * Business rules:
     *   - OPEN and IN_PROGRESS tickets can be resolved
     *   - CLOSED tickets cannot be resolved
     *   - RESOLVED tickets cannot be resolved again
     *   - Resolution text is mandatory
     *   - resolvedAt timestamp set automatically
     *
     * Resolution text is the engineer's documentation
     * of what was done. This feeds into AI report generation.
     */
    public TicketResponse resolveTicket(String id,
                                        ResolveTicketRequest request) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket", "id", id));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException(
                    "Cannot resolve a closed ticket.");
        }

        if (ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new BadRequestException(
                    "Ticket is already resolved.");
        }

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolution(request.getResolution());
        ticket.setResolvedAt(LocalDateTime.now());

        Ticket resolvedTicket = ticketRepository.save(ticket);

        auditService.log(
                SecurityContextHolder.getContext()
                        .getAuthentication().getName(),
                AuditAction.TICKET_RESOLVED, "Ticket",
                resolvedTicket.getId(), resolvedTicket.getTicketNumber(),
                "Resolved");

        log.info("Ticket {} resolved: {}",
                resolvedTicket.getTicketNumber(),
                request.getResolution().substring(0,
                        Math.min(50, request.getResolution().length())));

        return TicketResponse.fromTicket(resolvedTicket);
    }

    // ─────────────────────────────────────────
    // Close Ticket
    // ─────────────────────────────────────────

    /**
     * Close a resolved ticket.
     *
     * Business rules:
     *   - Only RESOLVED tickets can be closed
     *   - Cannot close OPEN or IN_PROGRESS tickets directly
     *     (must be resolved first)
     *   - CLOSED tickets cannot be closed again
     *   - closedAt timestamp set automatically
     *
     * Closing is a management action to confirm the
     * resolution has been reviewed and accepted.
     */
    public TicketResponse closeTicket(String id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket", "id", id));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException(
                    "Ticket is already closed.");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new BadRequestException(
                    "Only resolved tickets can be closed. " +
                            "Current status: " + ticket.getStatus());
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());

        Ticket closedTicket = ticketRepository.save(ticket);

        auditService.log(
                SecurityContextHolder.getContext()
                        .getAuthentication().getName(),
                AuditAction.TICKET_CLOSED, "Ticket",
                closedTicket.getId(), closedTicket.getTicketNumber(),
                "Ticket closed");

        log.info("Ticket {} closed.", closedTicket.getTicketNumber());

        return TicketResponse.fromTicket(closedTicket);
    }

    // ─────────────────────────────────────────
    // Dashboard Stats
    // ─────────────────────────────────────────

    /**
     * Count tickets by status.
     * Used by dashboard module.
     */
    public long countTicketsByStatus(TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    /**
     * Get 5 most recently created tickets.
     * Used for dashboard recent activity widget.
     */
    public List<TicketResponse> getRecentTickets() {
        return ticketRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());
    }

    /**
     * Get open and in-progress tickets.
     * Used for active work queue view.
     */
    public List<TicketResponse> getActiveTickets() {
        return ticketRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS))
                .stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────

    /**
     * Generate a unique human-readable ticket number.
     *
     * Format: TKT-YYYY-NNNNN
     * Example: TKT-2024-00042
     *
     * We get the total count of existing tickets and add 1.
     * The MongoDB unique index on ticketNumber ensures
     * no duplicates are actually saved even under load.
     */
    private String generateTicketNumber() {
        long count = ticketRepository.count();
        int year = Year.now().getValue();
        return String.format("TKT-%d-%05d", year, count + 1);
    }
}