package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ReactionJpaRepository extends JpaRepository<ReactionEntity, ReactionEntity.Key> {

    @Modifying
    @Query(value = """
            INSERT INTO reactions (user_id, showcase_id, type, created_at) VALUES (:userId, :showcaseId, 'LIKE', :at)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertLike(@Param("userId") UUID userId, @Param("showcaseId") UUID showcaseId, @Param("at") Instant at);

    @Modifying
    @Query(value = "DELETE FROM reactions WHERE user_id = :userId AND showcase_id = :showcaseId AND type = 'LIKE'", nativeQuery = true)
    int deleteLike(@Param("userId") UUID userId, @Param("showcaseId") UUID showcaseId);

    @Query(value = "SELECT count(*) FROM reactions WHERE showcase_id = :showcaseId AND type = 'LIKE'", nativeQuery = true)
    int countLikes(@Param("showcaseId") UUID showcaseId);

    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM reactions WHERE user_id = :userId AND showcase_id = :showcaseId AND type = 'LIKE')
            """, nativeQuery = true)
    boolean likeExists(@Param("userId") UUID userId, @Param("showcaseId") UUID showcaseId);

    // Locked, so a like given meanwhile cannot slip between this read and the delete below.
    @Query(value = "SELECT showcase_id FROM reactions WHERE user_id = :userId AND type = 'LIKE' FOR UPDATE", nativeQuery = true)
    List<UUID> lockLikedBy(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "DELETE FROM reactions WHERE user_id = :userId AND type = 'LIKE'", nativeQuery = true)
    int deleteLikesBy(@Param("userId") UUID userId);

    @Query(value = """
            SELECT showcase_id AS showcaseId, count(*) AS likes FROM reactions
            WHERE showcase_id IN (:showcaseIds) AND type = 'LIKE' GROUP BY showcase_id
            """, nativeQuery = true)
    List<LikeCount> countLikes(@Param("showcaseIds") Collection<UUID> showcaseIds);

    interface LikeCount {
        UUID getShowcaseId();

        long getLikes();
    }
}
