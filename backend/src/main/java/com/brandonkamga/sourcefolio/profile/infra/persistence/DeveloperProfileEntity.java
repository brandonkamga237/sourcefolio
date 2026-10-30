package com.brandonkamga.sourcefolio.profile.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "developer_profiles")
class DeveloperProfileEntity {

    @Id
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "country_code", length = 2, columnDefinition = "CHAR(2)")
    private String countryCode;

    @Column(length = 20)
    private String specialty;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DeveloperProfileEntity() {
    }

    DeveloperProfileEntity(UUID memberId) {
        this.memberId = memberId;
    }

    void update(String countryCode, String specialty, Instant updatedAt) {
        this.countryCode = countryCode;
        this.specialty = specialty;
        this.updatedAt = updatedAt;
    }

    UUID getMemberId() { return memberId; }
    String getCountryCode() { return countryCode; }
    String getSpecialty() { return specialty; }
    Instant getUpdatedAt() { return updatedAt; }
}
