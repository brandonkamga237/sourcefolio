package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    protected SourceRepoEntity() {
    }

    SourceRepoEntity(UUID showcaseId) {
        this.showcaseId = showcaseId;
    }

    void update(String provider, String owner, String name, boolean isPublic) {
        this.provider = provider;
        this.owner = owner;
        this.name = name;
        this.isPublic = isPublic;
    }

    String getProvider() { return provider; }
    String getOwner() { return owner; }
    String getName() { return name; }
    boolean isPublic() { return isPublic; }
}
