package com.theworkcode.audit.service;

import java.time.OffsetDateTime;

import com.theworkcode.audit.entity.AuditEventEntity;
import com.theworkcode.audit.repository.AuditEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Append-only audit log. Actors, actions, resources and correlation IDs are
 * recorded for every sensitive operation. No update/delete paths exist.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository repository;

    @Transactional
    public AuditEventEntity record(String actor, String actorRole, String action, String resourceType,
            String resourceId, String requestId, String result, String detail, String orgId) {
        AuditEventEntity e = new AuditEventEntity();
        e.setEventTime(OffsetDateTime.now());
        e.setActor(actor);
        e.setActorRole(actorRole);
        e.setAction(action);
        e.setResourceType(resourceType);
        e.setResourceId(resourceId);
        e.setRequestId(requestId);
        e.setResult(result == null ? "SUCCESS" : result);
        e.setDetail(detail);
        e.setOrgId(orgId);
        log.info("audit action={} actor={} resource={}", action, actor, resourceId);
        return repository.save(e);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventEntity> search(String action, String actor, String resourceId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "eventTime"));
        return repository.search(blankToNull(action), blankToNull(actor), blankToNull(resourceId), pageable);
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
