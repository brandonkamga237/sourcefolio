package com.brandonkamga.sourcefolio.account.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 2)
    private String language;

    private String email;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserEntity() {
    }

    UserEntity(UUID id, String language, String email, Instant createdAt) {
        this.id = id;
        this.language = language;
        this.email = email;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    String getLanguage() { return language; }
    String getEmail() { return email; }
    Instant getCreatedAt() { return createdAt; }
}
