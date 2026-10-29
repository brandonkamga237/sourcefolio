package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CandidateScanJpaRepository extends JpaRepository<CandidateScanEntity, UUID> {
}
