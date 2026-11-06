package com.brandonkamga.sourcefolio.contribution.infra.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ContributionRecordJpaRepository extends JpaRepository<ContributionRecordEntity, UUID> {

    // Two rows share a login only after a rename GitHub let someone else take: rare and few.
    @Query("SELECT r FROM ContributionRecordEntity r WHERE lower(r.login) = lower(:login) ORDER BY r.requestedAt DESC")
    List<ContributionRecordEntity> findByLogin(@Param("login") String login);

    @Query(value = """
            SELECT member_id FROM contribution_records
            WHERE GREATEST(analyzed_at, requested_at) < :before
            ORDER BY GREATEST(analyzed_at, requested_at), member_id
            LIMIT :limit""", nativeQuery = true)
    List<UUID> findDue(@Param("before") Instant before, @Param("limit") int limit);
}
