package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// Append-only: the history is written once and never changed.
@Entity
@Table(name = "showcase_status_events")
class StatusEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "showcase_id", nullable = false, updatable = false)
    private UUID showcaseId;

    @Column(name = "from_status", length = 20, updatable = false)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 20, updatable = false)
    private String toStatus;

    @Column(nullable = false, length = 40, updatable = false)
    private String reason;

    @Column(nullable = false, length = 20, updatable = false)
    private String actor;

    @Column(nullable = false, updatable = false)
    private Instant at;

    protected StatusEventEntity() {
    }

    StatusEventEntity(UUID showcaseId, String fromStatus, String toStatus, String reason, String actor, Instant at) {
        this.showcaseId = showcaseId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.actor = actor;
        this.at = at;
    }

}
