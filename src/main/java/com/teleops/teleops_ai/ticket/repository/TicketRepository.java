package com.teleops.teleops_ai.ticket.repository;

import com.teleops.teleops_ai.ticket.model.Ticket;
import com.teleops.teleops_ai.ticket.model.TicketPriority;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Ticket Repository
 *
 * Spring Data MongoDB auto-implements all methods.
 *
 * Key queries:
 *
 *   findByTicketNumber("TKT-2024-00001")
 *     → db.tickets.find({ ticketNumber: "TKT-2024-00001" })
 *     Used for human-readable ticket lookup.
 *
 *   findByStatus(OPEN)
 *     → db.tickets.find({ status: "OPEN" })
 *     Primary dashboard query.
 *
 *   findByAssignedTo(userId)
 *     → db.tickets.find({ assignedTo: userId })
 *     "My tickets" view for engineers.
 *
 *   countByStatus(OPEN)
 *     → db.tickets.count({ status: "OPEN" })
 *     Dashboard statistics.
 *
 *   findTop5ByOrderByCreatedAtDesc()
 *     → Most recent 5 tickets
 *     Dashboard recent activity widget.
 */
@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

    /**
     * Find ticket by human-readable ticket number.
     * Engineers reference tickets by this number.
     */
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    /**
     * All tickets by status.
     * Most common filter for ticket queue views.
     */
    List<Ticket> findByStatus(TicketStatus status);

    /**
     * All tickets by priority.
     */
    List<Ticket> findByPriority(TicketPriority priority);

    /**
     * All tickets assigned to a specific engineer.
     * Used for "my tickets" personal view.
     */
    List<Ticket> findByAssignedTo(String assignedTo);

    /**
     * All tickets for a specific device.
     * Used in device detail view to show ticket history.
     */
    List<Ticket> findByDeviceId(String deviceId);

    /**
     * All tickets linked to a specific alarm.
     * Used to show tickets raised from an alarm.
     */
    List<Ticket> findByAlarmId(String alarmId);

    /**
     * Filter by status AND priority.
     * Example: "all OPEN CRITICAL tickets".
     */
    List<Ticket> findByStatusAndPriority(TicketStatus status,
                                         TicketPriority priority);

    /**
     * Filter by status AND assignee.
     * Example: "all IN_PROGRESS tickets assigned to John".
     */
    List<Ticket> findByStatusAndAssignedTo(TicketStatus status,
                                           String assignedTo);

    /**
     * Count tickets by status.
     * Used for dashboard statistics.
     */
    long countByStatus(TicketStatus status);

    /**
     * Count tickets by priority.
     * Used for dashboard statistics.
     */
    long countByPriority(TicketPriority priority);

    /**
     * Total number of tickets.
     * Used to generate the next ticket number.
     */
    long count();

    /**
     * Most recent 5 tickets.
     * Used for dashboard "recent tickets" widget.
     */
    List<Ticket> findTop5ByOrderByCreatedAtDesc();

    /**
     * Open and in-progress tickets, most recent first.
     * Used for the main ticket queue.
     */
    List<Ticket> findByStatusInOrderByCreatedAtDesc(
            List<TicketStatus> statuses);
}