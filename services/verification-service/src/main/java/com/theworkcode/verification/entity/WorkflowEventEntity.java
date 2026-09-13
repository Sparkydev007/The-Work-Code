package com.theworkcode.verification.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "verification_workflow_events")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "step", nullable = false)
    private String step;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "detail")
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;
}
