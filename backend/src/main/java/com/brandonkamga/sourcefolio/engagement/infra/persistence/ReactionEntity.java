package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

// Written and counted by native statements in ReactionJpaRepository; mapped for schema validation.
@Entity
@Table(name = "reactions")
class ReactionEntity {

    @EmbeddedId
    private Key key;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReactionEntity() {
    }

    @Embeddable
    record Key(@Column(name = "user_id") UUID userId,
               @Column(name = "showcase_id") UUID showcaseId,
               @Column(length = 20) String type) implements Serializable {
    }
}
