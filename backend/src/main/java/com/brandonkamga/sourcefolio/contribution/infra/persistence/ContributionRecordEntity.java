package com.brandonkamga.sourcefolio.contribution.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contribution_records")
class ContributionRecordEntity {

    @Id
    @Column(name = "member_id")
    private UUID memberId;

    @Column(nullable = false, length = 39)
    private String login;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "analyzed_at")
    private Instant analyzedAt;

    @Column(name = "account_created_at")
    private Instant accountCreatedAt;

    @Column(name = "public_contributions")
    private Integer publicContributions;

    @Column(name = "active_months")
    private Short activeMonths;

    private Integer stars;

    private Integer followers;

    @Column(name = "merged_elsewhere")
    private Integer mergedElsewhere;

    @Column(columnDefinition = "text")
    private String details;

    protected ContributionRecordEntity() {
    }

    ContributionRecordEntity(UUID memberId) {
        this.memberId = memberId;
    }

    void update(String login, Instant requestedAt, Instant failedAt) {
        this.login = login;
        this.requestedAt = requestedAt;
        this.failedAt = failedAt;
    }

    void snapshot(Instant analyzedAt, Instant accountCreatedAt, Integer publicContributions, Short activeMonths,
                  Integer stars, Integer followers, Integer mergedElsewhere, String details) {
        this.analyzedAt = analyzedAt;
        this.accountCreatedAt = accountCreatedAt;
        this.publicContributions = publicContributions;
        this.activeMonths = activeMonths;
        this.stars = stars;
        this.followers = followers;
        this.mergedElsewhere = mergedElsewhere;
        this.details = details;
    }

    UUID getMemberId() { return memberId; }
    String getLogin() { return login; }
    Instant getRequestedAt() { return requestedAt; }
    Instant getFailedAt() { return failedAt; }
    Instant getAnalyzedAt() { return analyzedAt; }
    Instant getAccountCreatedAt() { return accountCreatedAt; }
    Integer getPublicContributions() { return publicContributions; }
    Integer getStars() { return stars; }
    Integer getFollowers() { return followers; }
    Integer getMergedElsewhere() { return mergedElsewhere; }
    String getDetails() { return details; }
}
