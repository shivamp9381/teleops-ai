package com.teleops.teleops_ai.audit.controller;

import com.teleops.teleops_ai.audit.model.AuditLog;
import com.teleops.teleops_ai.audit.service.AuditService;
import com.teleops.teleops_ai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Audit Log Controller
 *
 * Only SUPER_ADMIN and NOC_MANAGER can view audit logs.
 * Engineers cannot see who did what.
 */
@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
@Tag(name = "Audit Logs",
        description = "View system audit trail")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Get all audit logs (paginated)")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAllLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved.",
                        auditService.getAllLogs(page, size)));
    }

    @GetMapping("/user/{email}")
    @Operation(summary = "Get audit logs by user")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getByUser(
            @PathVariable String email,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("User audit logs retrieved.",
                        auditService.getLogsByUser(email, page, size)));
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "Get audit logs for a specific resource")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getByResource(
            @PathVariable String resourceId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Resource audit logs retrieved.",
                        auditService.getLogsByResource(
                                resourceId, page, size)));
    }
}