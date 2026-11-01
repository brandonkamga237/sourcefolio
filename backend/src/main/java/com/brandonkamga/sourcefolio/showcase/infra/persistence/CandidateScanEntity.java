package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    // VARCHAR(201)[] in the schema, "owner/name", most recently pushed first.
    @Column(name = "repositories_without_site", nullable = false, columnDefinition = "varchar(201)[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] repositoriesWithoutSite = new String[0];

    protected CandidateScanEntity() {
    }

    CandidateScanEntity(UUID memberId, Instant scannedAt, String[] repositoriesWithoutSite) {
        this.memberId = memberId;
        this.scannedAt = scannedAt;
        this.repositoriesWithoutSite = repositoriesWithoutSite;
    }

    UUID getMemberId() { return memberId; }
    Instant getScannedAt() { return scannedAt; }
    String[] getRepositoriesWithoutSite() { return repositoriesWithoutSite; }
}
