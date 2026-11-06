package com.brandonkamga.sourcefolio.contribution.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.contribution.domain.model.CalendarDay;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionRecord;
import com.brandonkamga.sourcefolio.contribution.domain.model.ContributionSnapshot;
import com.brandonkamga.sourcefolio.contribution.domain.model.ExternalProject;
import com.brandonkamga.sourcefolio.contribution.domain.model.GitHubActivity;
import com.brandonkamga.sourcefolio.contribution.domain.model.LanguageBytes;
import com.brandonkamga.sourcefolio.contribution.domain.model.OwnedRepository;
import com.brandonkamga.sourcefolio.contribution.domain.port.ContributionStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaContributionStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");

    @Autowired
    private ContributionStore records;

    @Autowired
    private UserStore users;

    @Autowired
    private EntityManager entityManager;

    private UUID member() {
        return users.save(User.register(Language.FR, NOW)).id().value();
    }

    @Test
    @DisplayName("a pending record, then its snapshot, read back exactly as stored")
    void roundTrip() {
        UUID member = member();
        records.save(ContributionRecord.requested(member, "nkeng-dev", NOW));
        flush();
        assertThat(records.find(member)).hasValueSatisfying(record -> {
            assertThat(record.login()).isEqualTo("nkeng-dev");
            assertThat(record.snapshot()).isNull();
        });

        ContributionSnapshot snapshot = ContributionSnapshot.of(new GitHubActivity(Instant.parse("2017-03-14T08:00:00Z"), "Nkeng Tabi",
                "Backend PHP", "Yaoundé", 94, 2_150, 10, List.of(new CalendarDay(LocalDate.parse("2026-11-05"), 3, 2)),
                List.of(new OwnedRepository("momo-gateway", "Payments", "https://github.com/n/momo-gateway", 500, "TypeScript",
                        List.of(new LanguageBytes("TypeScript", "#3178c6", 700)))),
                14, List.of(new ExternalProject("payafrica/momo-sdk", 6))), NOW.plusSeconds(4));
        records.save(records.find(member).orElseThrow().analyzed(snapshot));
        flush();

        assertThat(records.find(member)).hasValueSatisfying(record -> assertThat(record.snapshot()).isEqualTo(snapshot));
    }

    @Test
    @DisplayName("due members are those whose reading and request are both older than the limit, oldest first")
    void due() {
        UUID old = member();
        UUID older = member();
        UUID fresh = member();
        UUID askedRecently = member();
        records.save(ContributionRecord.requested(old, "old", NOW.minus(Duration.ofDays(9))));
        records.save(ContributionRecord.requested(older, "older", NOW.minus(Duration.ofDays(12))));
        records.save(ContributionRecord.requested(fresh, "fresh", NOW.minus(Duration.ofDays(1))));
        ContributionSnapshot snapshot = ContributionSnapshot.of(new GitHubActivity(NOW, null, null, null, 0, 0, 0, List.of(),
                List.of(), 0, List.of()), NOW.minus(Duration.ofDays(10)));
        records.save(ContributionRecord.requested(askedRecently, "asked", NOW.minus(Duration.ofDays(10))).analyzed(snapshot)
                .askedAgain("asked", NOW.minus(Duration.ofHours(1))));
        flush();

        assertThat(records.findDue(NOW.minus(Duration.ofDays(7)), 10)).containsExactly(older, old);
        assertThat(records.findDue(NOW.minus(Duration.ofDays(7)), 1)).containsExactly(older);
    }

    @Test
    @DisplayName("a record is found by login without case; after a rename the latest asked for wins")
    void byLogin() {
        UUID renamed = member();
        UUID current = member();
        records.save(ContributionRecord.requested(renamed, "Nkeng-Dev", NOW.minus(Duration.ofDays(30))));
        records.save(ContributionRecord.requested(current, "nkeng-dev", NOW));
        flush();

        assertThat(records.findByLogin("NKENG-DEV")).map(ContributionRecord::memberId).hasValue(current);
        assertThat(records.findByLogin("nobody")).isEmpty();
    }

    private void flush() {
        entityManager.flush();
        entityManager.clear();
    }
}
