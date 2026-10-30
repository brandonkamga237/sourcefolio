package com.brandonkamga.sourcefolio.profile.infra.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DeveloperProfileJpaRepository extends JpaRepository<DeveloperProfileEntity, UUID> {
}
