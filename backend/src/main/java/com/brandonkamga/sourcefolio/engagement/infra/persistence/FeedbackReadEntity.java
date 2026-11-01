package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback_reads")
class FeedbackReadEntity {

    @Id
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "seen_at", nullable = false)
    private Instant seenAt;

    protected FeedbackReadEntity() {
    }

    FeedbackReadEntity(UUID memberId, Instant seenAt) {
        this.memberId = memberId;
        this.seenAt = seenAt;
    }

    Instant getSeenAt() { return seenAt; }
}
