package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "showcase_candidate_scans")
class CandidateScanEntity {

    @Id
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;

    protected CandidateScanEntity() {
    }

    CandidateScanEntity(UUID memberId, Instant scannedAt) {
        this.memberId = memberId;
        this.scannedAt = scannedAt;
    }

    UUID getMemberId() { return memberId; }
    Instant getScannedAt() { return scannedAt; }
}
