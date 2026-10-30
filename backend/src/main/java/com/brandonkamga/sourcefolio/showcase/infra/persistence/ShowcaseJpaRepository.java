package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ShowcaseJpaRepository extends JpaRepository<ShowcaseEntity, UUID> {

    Optional<ShowcaseEntity> findFirstByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);

    Optional<ShowcaseEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
