package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback")
class FeedbackEntity {

    @Id
    private UUID id;

    @Column(name = "showcase_id", nullable = false)
    private UUID showcaseId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_login", nullable = false, length = 100)
    private String authorLogin;

    @Column(length = 20)
    private String angle;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(nullable = false, length = 5)
    private String language;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FeedbackEntity() {
    }

    FeedbackEntity(UUID id, UUID showcaseId, UUID authorId, String authorLogin, String angle, String body, String language,
                   Instant createdAt) {
        this.id = id;
        this.showcaseId = showcaseId;
        this.authorId = authorId;
        this.authorLogin = authorLogin;
        this.angle = angle;
        this.body = body;
        this.language = language;
        this.status = "VISIBLE";
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    UUID getShowcaseId() { return showcaseId; }
    UUID getAuthorId() { return authorId; }
    String getAuthorLogin() { return authorLogin; }
    String getAngle() { return angle; }
    String getBody() { return body; }
    String getLanguage() { return language; }
    Instant getCreatedAt() { return createdAt; }
}
