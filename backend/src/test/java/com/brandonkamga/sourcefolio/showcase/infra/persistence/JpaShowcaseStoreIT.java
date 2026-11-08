package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class JpaShowcaseStoreIT extends PostgresIT {

    @Autowired
    private ShowcaseStore store;

    @Autowired
    private UserStore users;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID member() {
        return users.save(User.register(Language.FR, Instant.parse("2026-11-08T10:00:00Z"))).id().value();
    }

    private void showcase(UUID owner, String imageKey, String videoKey) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO showcases (id, owner_user_id, kind, site_url, status, created_at, slug, preview_image_key, preview_video_key)
                VALUES (?, ?, 'PORTFOLIO', 'https://example.test', 'PUBLISHED', now(), ?, ?, ?)""",
                id, owner, id.toString(), imageKey, videoKey);
    }

    @Test
    @DisplayName("finds the image and video keys of one owner, and skips what was never uploaded")
    void findsTheKeysOfOneOwner() {
        UUID owner = member();
        UUID other = member();
        showcase(owner, "previews/image.webp", "previews/video.mp4");
        showcase(owner, "previews/alone.webp", null);
        showcase(other, "previews/other.webp", null);

        assertThat(store.findMediaKeysByOwner(owner))
                .containsExactlyInAnyOrder("previews/image.webp", "previews/video.mp4", "previews/alone.webp");
        assertThat(store.findMediaKeysByOwner(member())).isEmpty();
    }
}
