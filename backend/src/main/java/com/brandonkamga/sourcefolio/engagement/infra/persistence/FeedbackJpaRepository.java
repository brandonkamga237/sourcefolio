package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface FeedbackJpaRepository extends JpaRepository<FeedbackEntity, UUID> {

    // Keyset on (created_at, id), answered by the index feedback_showcase: never a deep OFFSET.
    @Query("""
            select f from FeedbackEntity f where f.showcaseId = :showcaseId and f.status = 'VISIBLE'
            order by f.createdAt desc, f.id desc
            """)
    List<FeedbackEntity> firstPage(@Param("showcaseId") UUID showcaseId, Limit limit);

    @Query("""
            select f from FeedbackEntity f
            where f.showcaseId = :showcaseId and f.status = 'VISIBLE'
              and (f.createdAt < :afterAt or (f.createdAt = :afterAt and f.id < :afterId))
            order by f.createdAt desc, f.id desc
            """)
    List<FeedbackEntity> pageAfter(@Param("showcaseId") UUID showcaseId, @Param("afterAt") Instant afterAt, @Param("afterId") UUID afterId,
                                   Limit limit);

    @Query("select count(f) from FeedbackEntity f where f.showcaseId = :showcaseId and f.status = 'VISIBLE'")
    int countVisible(@Param("showcaseId") UUID showcaseId);

    @Modifying
    @Query("update FeedbackEntity f set f.authorLogin = :login where f.authorId = :authorId")
    void renameAuthor(@Param("authorId") UUID authorId, @Param("login") String login);
}
