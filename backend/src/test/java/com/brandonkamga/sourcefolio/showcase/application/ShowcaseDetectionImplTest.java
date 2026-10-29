package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShowcaseDetectionImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    private final Map<String, List<PublicRepository>> github = new HashMap<>();
    private final InMemoryCandidateStore store = new InMemoryCandidateStore();
    private final List<String> scheduled = new ArrayList<>();
    private final ShowcaseDetection detection = new ShowcaseDetectionImpl(
            login -> github.getOrDefault(login, List.of()), store,
            (memberId, login) -> scheduled.add(memberId + " " + login), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("requesting a detection schedules it in the background and touches nothing else")
    void requestSchedules() {
        detection.requestDetection(MEMBER, "nekwalla");

        assertThat(scheduled).containsExactly(MEMBER + " nekwalla");
        assertThat(store.rows).isEmpty();
    }

    @Test
    @DisplayName("a detection stores the ranked candidates and when it ran")
    void detectStores() {
        github.put("nekwalla", List.of(
                repo("api", "https://api.example.org"),
                repo("nekwalla.github.io", null)));

        detection.detect(MEMBER, "nekwalla");

        CandidateScan scan = store.rows.get(MEMBER);
        assertThat(scan.scannedAt()).isEqualTo(NOW);
        assertThat(scan.candidates()).extracting(ShowcaseCandidate::reason)
                .containsExactly(CandidateReason.USER_PAGES, CandidateReason.HOMEPAGE);
    }

    @Test
    @DisplayName("a member with no candidate gets an empty scan, so the screen can say nothing was found")
    void nothingFoundIsStillAScan() {
        detection.detect(MEMBER, "nekwalla");

        assertThat(detection.findCandidates(MEMBER)).hasValueSatisfying(scan -> assertThat(scan.candidates()).isEmpty());
    }

    @Test
    @DisplayName("running a detection again replaces the previous result instead of adding to it")
    void detectIsIdempotent() {
        github.put("nekwalla", List.of(repo("portfolio", "https://nekwalla.dev")));
        detection.detect(MEMBER, "nekwalla");
        detection.detect(MEMBER, "nekwalla");

        assertThat(store.rows.get(MEMBER).candidates()).hasSize(1);
    }

    @Test
    @DisplayName("before the first detection finishes, there is no scan at all")
    void noScanYet() {
        assertThat(detection.findCandidates(MEMBER)).isEmpty();
    }

    private static PublicRepository repo(String name, String homepage) {
        return new PublicRepository(new RepositoryCoordinates("nekwalla", name), homepage, false, NOW);
    }

    static class InMemoryCandidateStore implements CandidateStore {
        final Map<UUID, CandidateScan> rows = new HashMap<>();

        @Override
        public void replace(UUID memberId, CandidateScan scan) {
            rows.put(memberId, scan);
        }

        @Override
        public Optional<CandidateScan> find(UUID memberId) {
            return Optional.ofNullable(rows.get(memberId));
        }
    }
}
