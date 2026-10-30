package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SourceRepoJpaRepository extends JpaRepository<SourceRepoEntity, UUID> {

    // Same expressions as the unique index source_repos_repository, so the index answers it.
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM source_repos
                WHERE provider = :provider AND lower(owner) = lower(:owner) AND lower(name) = lower(:name))
            """, nativeQuery = true)
    boolean existsRepository(@Param("provider") String provider, @Param("owner") String owner, @Param("name") String name);

    // Read longest ago first, never read before all (index source_repos_stars_checked_at).
    @Query(value = """
            SELECT r.showcase_id FROM source_repos r JOIN showcases s ON s.id = r.showcase_id
            WHERE s.status <> 'REMOVED' AND (r.stars_checked_at IS NULL OR r.stars_checked_at < :readBefore)
            ORDER BY r.stars_checked_at NULLS FIRST, r.showcase_id
            LIMIT :limit
            """, nativeQuery = true)
    List<UUID> dueForStars(@Param("readBefore") Instant readBefore, @Param("limit") int limit);

    @Modifying
    @Query("update SourceRepoEntity r set r.stars = :stars, r.starsCheckedAt = :checkedAt where r.showcaseId = :showcaseId")
    int saveStars(@Param("showcaseId") UUID showcaseId, @Param("stars") Integer stars, @Param("checkedAt") Instant checkedAt);
}
