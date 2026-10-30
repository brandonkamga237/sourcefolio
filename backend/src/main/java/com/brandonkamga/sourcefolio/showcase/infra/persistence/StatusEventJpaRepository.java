package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface StatusEventJpaRepository extends JpaRepository<StatusEventEntity, Long> {
}
