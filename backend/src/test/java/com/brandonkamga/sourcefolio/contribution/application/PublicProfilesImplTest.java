package com.brandonkamga.sourcefolio.contribution.application;

import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import com.brandonkamga.sourcefolio.profile.application.PublicProfile;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublicProfilesImplTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");

    private final InMemoryContributionStore records = new InMemoryContributionStore();
    private final Map<UUID, PublicProfile> directory = new HashMap<>();
    private final PublicProfilesImpl profiles = new PublicProfilesImpl(records, memberId -> Optional.ofNullable(directory.get(memberId)));

    private static ContributionSnapshot snapshot() {
        return ContributionSnapshot.of(new GitHubActivity(NOW, "Nkeng", null, null, 3, 10, 0, List.of(), List.of(), 0, List.of()), NOW);
    }

    private static PublicProfile profile(boolean published) {
        return new PublicProfile("CM", List.of(), null, null, "Yaoundé", "Backend", "https://www.linkedin.com/in/nkeng", published);
    }

    @Test
    @DisplayName("a published profile with a snapshot is found by login, whatever its case")
    void published() {
        records.save(ContributionRecord.requested(MEMBER, "Nkeng-Dev", NOW).analyzed(snapshot()));
        directory.put(MEMBER, profile(true));

        assertThat(profiles.findByLogin("nkeng-dev")).hasValueSatisfying(card -> {
            assertThat(card.login()).isEqualTo("Nkeng-Dev");
            assertThat(card.profile().city()).isEqualTo("Yaoundé");
            assertThat(card.snapshot().followers()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("nothing is public without consent, nor before the first reading")
    void hidden() {
        records.save(ContributionRecord.requested(MEMBER, "nkeng-dev", NOW).analyzed(snapshot()));
        directory.put(MEMBER, profile(false));
        assertThat(profiles.findByLogin("nkeng-dev")).isEmpty();

        UUID pending = UUID.fromString("1c9f3d2f-5e7b-4a7f-8b88-4d3e2f1a0b22");
        records.save(ContributionRecord.requested(pending, "pending", NOW));
        directory.put(pending, profile(true));
        assertThat(profiles.findByLogin("pending")).isEmpty();
        assertThat(profiles.findByLogin("nobody")).isEmpty();
    }
}
