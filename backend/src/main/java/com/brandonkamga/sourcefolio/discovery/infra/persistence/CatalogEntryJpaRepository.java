package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CatalogEntryJpaRepository extends JpaRepository<CatalogEntryEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO catalog_entries (showcase_id, slug, site_url, repository, repository_url, owner_user_id, owner_login, published_at)
            VALUES (:showcaseId, :slug, :siteUrl, :repository, :repositoryUrl, :ownerUserId, :ownerLogin, :publishedAt)
            ON CONFLICT (showcase_id) DO UPDATE SET
                slug = EXCLUDED.slug, site_url = EXCLUDED.site_url, repository = EXCLUDED.repository,
                repository_url = EXCLUDED.repository_url, owner_login = EXCLUDED.owner_login,
                published_at = EXCLUDED.published_at
            """, nativeQuery = true)
    void upsert(@Param("showcaseId") UUID showcaseId, @Param("slug") String slug, @Param("siteUrl") String siteUrl,
                @Param("repository") String repository, @Param("repositoryUrl") String repositoryUrl,
                @Param("ownerUserId") UUID ownerUserId, @Param("ownerLogin") String ownerLogin,
                @Param("publishedAt") Instant publishedAt);

    @Modifying
    @Query("update CatalogEntryEntity e set e.previewImageUrl = :imageUrl, e.previewVideoUrl = :videoUrl where e.showcaseId = :showcaseId")
    void setPreview(@Param("showcaseId") UUID showcaseId, @Param("imageUrl") String imageUrl, @Param("videoUrl") String videoUrl);

    @Modifying
    @Query("update CatalogEntryEntity e set e.likeCount = :count where e.showcaseId = :showcaseId")
    void setLikes(@Param("showcaseId") UUID showcaseId, @Param("count") int count);

    @Modifying
    @Query("update CatalogEntryEntity e set e.repositoryStars = :stars where e.showcaseId = :showcaseId")
    void setRepositoryStars(@Param("showcaseId") UUID showcaseId, @Param("stars") Integer stars);

    @Query("""
            select e from CatalogEntryEntity e where e.likeCount > 0
            order by e.likeCount desc, e.publishedAt desc, e.showcaseId desc
            """)
    List<CatalogEntryEntity> mostLiked(Limit limit);

    @Modifying
    @Query("update CatalogEntryEntity e set e.ownerLogin = :login where e.ownerUserId = :ownerUserId")
    void renameOwner(@Param("ownerUserId") UUID ownerUserId, @Param("login") String login);

    // The shuffle key is computed per row, so this sorts the whole young or old list: fine for a
    // catalogue of thousands. A stored key per day becomes worth it well beyond that.
    @Query(value = """
            SELECT CAST(showcase_id AS VARCHAR) || ':' || md5(CAST(showcase_id AS VARCHAR) || :day) AS id_and_key
            FROM catalog_entries
            WHERE (published_at >= :youngSince) = :young
              AND md5(CAST(showcase_id AS VARCHAR) || :day) > :afterKey
            ORDER BY md5(CAST(showcase_id AS VARCHAR) || :day)
            LIMIT :limit
            """, nativeQuery = true)
    List<String> shuffledIdsAndKeys(@Param("day") String day, @Param("young") boolean young,
                                    @Param("youngSince") Instant youngSince, @Param("afterKey") String afterKey,
                                    @Param("limit") int limit);

    List<CatalogEntryEntity> findAllByOrderByPublishedAtDescShowcaseIdDesc(Limit limit);

    @Query("""
            select e from CatalogEntryEntity e
            where e.publishedAt < :publishedAt or (e.publishedAt = :publishedAt and e.showcaseId < :showcaseId)
            order by e.publishedAt desc, e.showcaseId desc
            """)
    List<CatalogEntryEntity> recentAfter(@Param("publishedAt") Instant publishedAt, @Param("showcaseId") UUID showcaseId, Limit limit);
}
