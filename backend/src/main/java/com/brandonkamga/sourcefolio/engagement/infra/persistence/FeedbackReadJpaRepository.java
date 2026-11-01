package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface FeedbackReadJpaRepository extends JpaRepository<FeedbackReadEntity, UUID> {
}
