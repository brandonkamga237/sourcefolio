package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

// Rows are only ever inserted (a scan replaces the list): Persistable tells Spring Data so,
// otherwise an assigned id makes saveAll run one SELECT per row before inserting it.
@Entity
@Table(name = "showcase_candidates")
class CandidateEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private short position;

    @Column(name = "repository_owner", nullable = false)
    private String repositoryOwner;

    @Column(name = "repository_name", nullable = false)
    private String repositoryName;

    @Column(name = "site_url", nullable = false)
    private String siteUrl;

    @Column(nullable = false, length = 20)
    private String reason;

    @Transient
    private boolean fresh;

    protected CandidateEntity() {
    }

    CandidateEntity(UUID id, UUID memberId, short position, String repositoryOwner, String repositoryName,
                    String siteUrl, String reason) {
        this.id = id;
        this.memberId = memberId;
        this.position = position;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.siteUrl = siteUrl;
        this.reason = reason;
        this.fresh = true;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return fresh;
    }

    String getRepositoryOwner() { return repositoryOwner; }
    String getRepositoryName() { return repositoryName; }
    String getSiteUrl() { return siteUrl; }
    String getReason() { return reason; }
}
