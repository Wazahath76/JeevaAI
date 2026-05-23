package com.medicore.hms.service;

import com.medicore.hms.entity.AuditLog;
import com.medicore.hms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
        String entityType,
        UUID entityId,
        String action,
        String oldValue,
        String newValue,
        String description
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String actorName = (auth != null) ? auth.getName() : "system";
            String actorRole = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority()
                : "SYSTEM";

            AuditLog entry = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actorName(actorName)
                .actorRole(actorRole)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit must never break the main flow
            log.error("Failed to write audit log for entity [{}/{}]: {}", entityType, entityId, e.getMessage());
        }
    }
}
