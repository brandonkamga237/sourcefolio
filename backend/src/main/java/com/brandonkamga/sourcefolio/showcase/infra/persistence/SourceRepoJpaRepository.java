package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
