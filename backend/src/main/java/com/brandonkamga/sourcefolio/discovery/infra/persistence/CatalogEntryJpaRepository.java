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

    // The visitor's optional filters, shared by the shuffle, Recent and the count. A null part
    // means "any"; the casts tell PostgreSQL the type of a parameter bound as null.
    String FILTERS = """
             AND (CAST(:country AS VARCHAR) IS NULL OR country_code = CAST(:country AS CHAR(2)))
             AND (CAST(:specialty AS VARCHAR) IS NULL OR CAST(:specialty AS VARCHAR) = ANY(specialties))
             AND (CAST(:pattern AS VARCHAR) IS NULL
                  OR lower(owner_login) LIKE CAST(:pattern AS VARCHAR)
                  OR lower(site_url) LIKE CAST(:pattern AS VARCHAR)
                  OR lower(coalesce(headline, '')) LIKE CAST(:pattern AS VARCHAR))
            """;

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

    // lower() on both sides: the expression index catalog_entries_owner_login answers it.
    @Query("select e from CatalogEntryEntity e where lower(e.ownerLogin) = lower(:login) order by e.publishedAt desc")
    List<CatalogEntryEntity> byOwnerLogin(@Param("login") String login);

    @Modifying
    @Query("""
            update CatalogEntryEntity e set e.countryCode = :countryCode, e.specialties = :specialties, e.headline = :headline
            where e.ownerUserId = :ownerId""")
    void setAuthorProfile(@Param("ownerId") UUID ownerId, @Param("countryCode") String countryCode, @Param("specialties") String[] specialties,
                          @Param("headline") String headline);

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
            """ + FILTERS + """
            ORDER BY md5(CAST(showcase_id AS VARCHAR) || :day)
            LIMIT :limit
            """, nativeQuery = true)
    List<String> shuffledIdsAndKeys(@Param("day") String day, @Param("young") boolean young,
                                    @Param("youngSince") Instant youngSince, @Param("afterKey") String afterKey,
                                    @Param("limit") int limit, @Param("country") String country,
                                    @Param("specialty") String specialty, @Param("pattern") String pattern);

    // Keyset on (published_at, showcase_id): never a deep OFFSET. Ids only; the rows follow.
    @Query(value = """
            SELECT showcase_id FROM catalog_entries
            WHERE (CAST(:afterAt AS TIMESTAMPTZ) IS NULL
                   OR published_at < CAST(:afterAt AS TIMESTAMPTZ)
                   OR (published_at = CAST(:afterAt AS TIMESTAMPTZ) AND showcase_id < CAST(:afterId AS UUID)))
            """ + FILTERS + """
            ORDER BY published_at DESC, showcase_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<UUID> recentIds(@Param("afterAt") Instant afterAt, @Param("afterId") UUID afterId, @Param("limit") int limit,
                         @Param("country") String country, @Param("specialty") String specialty, @Param("pattern") String pattern);

    @Query(value = """
            SELECT country_code AS value, count(*) AS count FROM catalog_entries
            WHERE country_code IS NOT NULL GROUP BY country_code ORDER BY count DESC, value
            """, nativeQuery = true)
    List<FacetRow> countryFacets();

    @Query(value = """
            SELECT interest AS value, count(*) AS count FROM catalog_entries, unnest(specialties) AS interest
            GROUP BY interest ORDER BY count DESC, value
            """, nativeQuery = true)
    List<FacetRow> specialtyFacets();

    interface FacetRow {
        String getValue();

        long getCount();
    }

    @Query(value = "SELECT count(*) FROM catalog_entries WHERE TRUE" + FILTERS, nativeQuery = true)
    long countMatching(@Param("country") String country, @Param("specialty") String specialty, @Param("pattern") String pattern);

}
