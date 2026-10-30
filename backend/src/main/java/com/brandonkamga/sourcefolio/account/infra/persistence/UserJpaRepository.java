package com.brandonkamga.sourcefolio.account.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
}
