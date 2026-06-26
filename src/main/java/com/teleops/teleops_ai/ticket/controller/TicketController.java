package com.teleops.teleops_ai.ticket.controller;

import com.teleops.teleops_ai.common.response.ApiResponse;
import com.teleops.teleops_ai.ticket.dto.AssignTicketRequest;
import com.teleops.teleops_ai.ticket.dto.ResolveTicketRequest;
import com.teleops.teleops_ai.ticket.dto.TicketRequest;
import com.teleops.teleops_ai.ticket.dto.TicketResponse;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import com.teleops.teleops_ai.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ticket Controller
 *
 * REST endpoints for the full ticket lifecycle.
 *
 * Access control summary:
 *   POST   /tickets              = NOC_ENGINEER+  (create)
 *   GET    /tickets              = all auth        (view all)
 *   GET    /tickets/active       = all auth        (active only)
 *   GET    /tickets/my           = NOC_ENGINEER+  (personal queue)
 *   GET    /tickets/status/{s}   = all auth        (filter by status)
 *   GET    /tickets/device/{id}  = all auth        (device history)
 *   GET    /tickets/{id}         = all auth        (detail)
 *   PATCH  /tickets/{id}/assign  = NOC_MANAGER+   (assign)
 *   PATCH  /tickets/{id}/resolve = NOC_ENGINEER+  (resolve)
 *   PATCH  /tickets/{id}/close   = NOC_MANAGER+   (close)
 *
 * URL ordering:
 *   /tickets/active, /tickets/my, /tickets/status/{s},
 *   /tickets/device/{id} MUST come before /tickets/{id}
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Ticket Management",
        description = "Create, assign, resolve and close incident tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * POST /api/v1/tickets
     * Create a new incident ticket.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Create ticket",
            description = "Create a new incident ticket linked to a device and optionally an alarm."
    )
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody TicketRequest request) {

        TicketResponse response = ticketService.createTicket(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Ticket created successfully.", response));
    }

    /**
     * GET /api/v1/tickets
     * Get all tickets.
     */
    @GetMapping
    @Operation(
            summary = "Get all tickets",
            description = "Retrieve complete ticket list."
    )
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getAllTickets() {

        List<TicketResponse> tickets = ticketService.getAllTickets();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tickets retrieved successfully.", tickets));
    }

    /**
     * GET /api/v1/tickets/active
     * Get open and in-progress tickets.
     * MUST be before /{id}
     */
    @GetMapping("/active")
    @Operation(
            summary = "Get active tickets",
            description = "Get all OPEN and IN_PROGRESS tickets ordered by creation date."
    )
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getActiveTickets() {

        List<TicketResponse> tickets = ticketService.getActiveTickets();

        return ResponseEntity.ok(
                ApiResponse.success("Active tickets retrieved.", tickets));
    }

    /**
     * GET /api/v1/tickets/my
     * Get tickets assigned to the current user.
     * MUST be before /{id}
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Get my tickets",
            description = "Get all tickets assigned to the currently authenticated engineer."
    )
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets() {

        List<TicketResponse> tickets = ticketService.getMyTickets();

        return ResponseEntity.ok(
                ApiResponse.success("Your tickets retrieved.", tickets));
    }

    /**
     * GET /api/v1/tickets/status/{status}
     * Filter tickets by status.
     * MUST be before /{id}
     */
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get tickets by status",
            description = "Filter tickets by: OPEN, IN_PROGRESS, RESOLVED, CLOSED"
    )
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByStatus(
            @PathVariable TicketStatus status) {

        List<TicketResponse> tickets =
                ticketService.getTicketsByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tickets by status retrieved.", tickets));
    }

    /**
     * GET /api/v1/tickets/device/{deviceId}
     * Get all tickets for a specific device.
     * MUST be before /{id}
     */
    @GetMapping("/device/{deviceId}")
    @Operation(
            summary = "Get tickets by device",
            description = "Get ticket history for a specific device."
    )
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByDevice(
            @PathVariable String deviceId) {

        List<TicketResponse> tickets =
                ticketService.getTicketsByDevice(deviceId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device tickets retrieved.", tickets));
    }

    /**
     * GET /api/v1/tickets/{id}
     * Get a single ticket by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get ticket by ID",
            description = "Get full ticket details including incident report if generated."
    )
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @PathVariable String id) {

        TicketResponse response = ticketService.getTicketById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ticket retrieved successfully.", response));
    }

    /**
     * PATCH /api/v1/tickets/{id}/assign
     * Assign ticket to an engineer.
     * Manager only.
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Assign ticket",
            description = "Assign a ticket to a specific engineer. Moves status to IN_PROGRESS."
    )
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable String id,
            @Valid @RequestBody AssignTicketRequest request) {

        TicketResponse response = ticketService.assignTicket(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ticket assigned successfully.", response));
    }

    /**
     * PATCH /api/v1/tickets/{id}/resolve
     * Resolve a ticket with documentation.
     * Engineer and above.
     */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Resolve ticket",
            description = "Resolve a ticket with mandatory resolution documentation."
    )
    public ResponseEntity<ApiResponse<TicketResponse>> resolveTicket(
            @PathVariable String id,
            @Valid @RequestBody ResolveTicketRequest request) {

        TicketResponse response = ticketService.resolveTicket(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ticket resolved successfully.", response));
    }

    /**
     * PATCH /api/v1/tickets/{id}/close
     * Close a resolved ticket.
     * Manager only.
     */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Close ticket",
            description = "Close a resolved ticket after management review."
    )
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(
            @PathVariable String id) {

        TicketResponse response = ticketService.closeTicket(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ticket closed successfully.", response));
    }
}