package com.brandonkamga.sourcefolio.account.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The link between a member and an account at a provider. The provider's numeric id is what
 * identifies the person: the login is only kept to display it and follows renames.
 */
public record AuthIdentity(UUID id, UserId userId, AuthProvider provider, String providerUserId, String login,
                           Instant createdAt, Instant updatedAt) {

    public AuthIdentity {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(providerUserId, "providerUserId");
        Objects.requireNonNull(login, "login");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static AuthIdentity link(UserId userId, AuthProvider provider, String providerUserId, String login, Instant now) {
        return new AuthIdentity(UUID.randomUUID(), userId, provider, providerUserId, login, now, now);
    }

    public boolean hasLogin(String candidate) {
        return login.equals(candidate);
    }

    public AuthIdentity renamedTo(String newLogin, Instant now) {
        return new AuthIdentity(id, userId, provider, providerUserId, newLogin, createdAt, now);
    }
}
