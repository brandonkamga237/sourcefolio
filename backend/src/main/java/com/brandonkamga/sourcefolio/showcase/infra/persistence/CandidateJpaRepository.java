package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface CandidateJpaRepository extends JpaRepository<CandidateEntity, UUID> {

    List<CandidateEntity> findByMemberIdOrderByPosition(UUID memberId);

    // One statement: a derived deleteBy would load every row and delete them one by one.
    @Modifying
    @Query("delete from CandidateEntity candidate where candidate.memberId = :memberId")
    void deleteAllOfMember(@Param("memberId") UUID memberId);
}
