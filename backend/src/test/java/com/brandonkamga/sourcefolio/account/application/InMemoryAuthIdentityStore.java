package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.AuthIdentity;
import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.AuthIdentityStore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryAuthIdentityStore implements AuthIdentityStore {

    final Map<UUID, AuthIdentity> rows = new LinkedHashMap<>();

    @Override
    public AuthIdentity save(AuthIdentity identity) {
        rows.put(identity.id(), identity);
        return identity;
    }

    @Override
    public Optional<AuthIdentity> find(AuthProvider provider, String providerUserId) {
        return rows.values().stream()
                .filter(row -> row.provider() == provider && row.providerUserId().equals(providerUserId))
                .findFirst();
    }

    @Override
    public Optional<AuthIdentity> findByUser(UserId userId, AuthProvider provider) {
        return rows.values().stream()
                .filter(row -> row.userId().equals(userId) && row.provider() == provider)
                .findFirst();
    }
}
