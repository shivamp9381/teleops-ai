package com.teleops.teleops_ai.dashboard.controller;

import com.teleops.teleops_ai.common.response.ApiResponse;
import com.teleops.teleops_ai.dashboard.dto.DashboardStats;
import com.teleops.teleops_ai.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard Controller
 *
 * Provides the NOC dashboard with aggregated statistics.
 *
 * All dashboard endpoints are accessible to all authenticated users.
 * READ_ONLY users need to see the dashboard.
 *
 * Single endpoint for now:
 *   GET /dashboard/stats → Full NOC overview
 *
 * The frontend calls this on page load and polls every 60 seconds.
 * Redis cache ensures only one MongoDB query per 60 seconds
 * regardless of how many engineers are watching the dashboard.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard",
        description = "NOC dashboard statistics and overview")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET /api/v1/dashboard/stats
     *
     * Returns the full NOC statistics overview:
     *   - Device counts by status
     *   - Alarm counts by severity
     *   - Ticket counts by status
     *
     * Response is cached in Redis for 60 seconds.
     * The generatedAt field in the response shows
     * when the data was last computed.
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Get dashboard statistics",
            description = "Returns aggregated NOC statistics. " +
                    "Data is cached for 60 seconds. " +
                    "Check generatedAt field for data freshness."
    )
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {

        DashboardStats stats = dashboardService.getDashboardStats();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard statistics retrieved.", stats));
    }
}