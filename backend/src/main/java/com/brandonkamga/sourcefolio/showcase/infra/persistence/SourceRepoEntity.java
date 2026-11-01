package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "source_repos")
class SourceRepoEntity {

    @Id
    @Column(name = "showcase_id")
    private UUID showcaseId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false, length = 100)
    private String owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    private Integer stars;

    @Column(name = "stars_checked_at")
    private Instant starsCheckedAt;

    @Column(length = 350)
    private String description;

    // VARCHAR(40)[] in the schema, largest share first.
    @Column(name = "languages", nullable = false, columnDefinition = "varchar(40)[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] languages = new String[0];

    @Column(name = "pushed_at")
    private Instant pushedAt;

    protected SourceRepoEntity() {
    }

    SourceRepoEntity(UUID showcaseId) {
        this.showcaseId = showcaseId;
    }

    void update(String provider, String owner, String name, boolean isPublic, Integer stars, Instant starsCheckedAt) {
        this.provider = provider;
        this.owner = owner;
        this.name = name;
        this.isPublic = isPublic;
        this.stars = stars;
        this.starsCheckedAt = starsCheckedAt;
    }

    UUID getShowcaseId() { return showcaseId; }
    String getProvider() { return provider; }
    String getOwner() { return owner; }
    String getName() { return name; }
    boolean isPublic() { return isPublic; }
    Integer getStars() { return stars; }
    Instant getStarsCheckedAt() { return starsCheckedAt; }
    String getDescription() { return description; }
    String[] getLanguages() { return languages; }
    Instant getPushedAt() { return pushedAt; }
}
