package com.brandonkamga.sourcefolio.account.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface AuthIdentityJpaRepository extends JpaRepository<AuthIdentityEntity, UUID> {

    Optional<AuthIdentityEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<AuthIdentityEntity> findByUserIdAndProvider(UUID userId, String provider);
}
