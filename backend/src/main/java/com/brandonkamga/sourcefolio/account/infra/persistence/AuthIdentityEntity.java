package com.brandonkamga.sourcefolio.account.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

// user_id is a plain column, not an association: the two aggregates reference each other by id.
@Entity
@Table(name = "auth_identities")
class AuthIdentityEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(nullable = false)
    private String login;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AuthIdentityEntity() {
    }

    AuthIdentityEntity(UUID id, UUID userId, String provider, String providerUserId, String login,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.login = login;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    String getProvider() { return provider; }
    String getProviderUserId() { return providerUserId; }
    String getLogin() { return login; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
}
