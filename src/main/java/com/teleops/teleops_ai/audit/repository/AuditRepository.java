package com.teleops.teleops_ai.audit.repository;

import com.teleops.teleops_ai.audit.model.AuditAction;
import com.teleops.teleops_ai.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends MongoRepository<AuditLog, String> {

    Page<AuditLog> findByUserEmailOrderByTimestampDesc(
            String userEmail, Pageable pageable);

    Page<AuditLog> findByActionOrderByTimestampDesc(
            AuditAction action, Pageable pageable);

    Page<AuditLog> findByResourceIdOrderByTimestampDesc(
            String resourceId, Pageable pageable);

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end);

    long countByAction(AuditAction action);

    long countByUserEmailAndTimestampAfter(
            String userEmail, LocalDateTime after);
}