package com.brandonkamga.sourcefolio.account.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.AuthIdentity;
import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.AuthIdentityStore;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class JpaAuthIdentityStore implements AuthIdentityStore {

    private final AuthIdentityJpaRepository repository;

    JpaAuthIdentityStore(AuthIdentityJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthIdentity save(AuthIdentity identity) {
        return toDomain(repository.save(new AuthIdentityEntity(identity.id(), identity.userId().value(), identity.provider().name(),
                identity.providerUserId(), identity.login(), identity.createdAt(), identity.updatedAt())));
    }

    @Override
    public Optional<AuthIdentity> find(AuthProvider provider, String providerUserId) {
        return repository.findByProviderAndProviderUserId(provider.name(), providerUserId).map(JpaAuthIdentityStore::toDomain);
    }

    @Override
    public Optional<AuthIdentity> findByUser(UserId userId, AuthProvider provider) {
        return repository.findByUserIdAndProvider(userId.value(), provider.name()).map(JpaAuthIdentityStore::toDomain);
    }

    private static AuthIdentity toDomain(AuthIdentityEntity entity) {
        return new AuthIdentity(entity.getId(), new UserId(entity.getUserId()), AuthProvider.valueOf(entity.getProvider()),
                entity.getProviderUserId(), entity.getLogin(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
