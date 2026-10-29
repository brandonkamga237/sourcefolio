package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaCandidateStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");

    @Autowired
    private CandidateStore candidates;

    @Autowired
    private UserStore users;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("a scan is stored and read back with its candidates in order")
    void roundTrip() {
        UUID member = member();
        CandidateScan scan = new CandidateScan(NOW, List.of(
                candidate("nekwalla.github.io", "https://nekwalla.github.io/", CandidateReason.USER_PAGES),
                candidate("api", "https://api.example.org/", CandidateReason.HOMEPAGE)));

        candidates.replace(member, scan);
        entityManager.flush();
        entityManager.clear();

        assertThat(candidates.find(member)).contains(scan);
    }

    @Test
    @DisplayName("a new scan replaces the previous one entirely, including an empty result")
    void replaceRemovesPreviousCandidates() {
        UUID member = member();
        candidates.replace(member, new CandidateScan(NOW, List.of(
                candidate("site", "https://nekwalla.dev/", CandidateReason.NAMED_REPOSITORY))));
        entityManager.flush();

        candidates.replace(member, new CandidateScan(NOW.plusSeconds(60), List.of()));
        entityManager.flush();
        entityManager.clear();

        assertThat(candidates.find(member)).hasValueSatisfying(scan -> {
            assertThat(scan.scannedAt()).isEqualTo(NOW.plusSeconds(60));
            assertThat(scan.candidates()).isEmpty();
        });
    }

    @Test
    @DisplayName("a member never scanned has no scan")
    void neverScanned() {
        assertThat(candidates.find(member())).isEmpty();
    }

    private UUID member() {
        return users.save(User.register(Language.FR, NOW)).id().value();
    }

    private static ShowcaseCandidate candidate(String name, String site, CandidateReason reason) {
        return new ShowcaseCandidate(new RepositoryCoordinates("nekwalla", name), new SiteUrl(site), reason);
    }
}
