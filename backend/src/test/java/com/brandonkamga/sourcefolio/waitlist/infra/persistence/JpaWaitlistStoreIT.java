package com.brandonkamga.sourcefolio.waitlist.infra.persistence;

import com.brandonkamga.sourcefolio.shared.domain.EmailAddress;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import com.brandonkamga.sourcefolio.waitlist.domain.port.WaitlistStore;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaWaitlistStoreIT extends PostgresIT {

    @Autowired
    private WaitlistStore store;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("the same address, whatever its capitals, stays one row with its first consent date")
    void oneRowPerAddress() {
        String local = "ada-" + UUID.randomUUID();
        Instant first = Instant.parse("2026-11-08T10:00:00Z");

        store.add(new EmailAddress(local + "@acme.africa"), first);
        store.add(new EmailAddress(local.toUpperCase() + "@ACME.africa"), Instant.parse("2026-11-09T10:00:00Z"));

        List<Instant> dates = jdbc.query("SELECT consented_at FROM waitlist_signups WHERE lower(email) = ?",
                (row, index) -> row.getObject(1, Timestamp.class).toInstant(), local + "@acme.africa");
        assertThat(dates).containsExactly(first);

        store.remove(new EmailAddress(local.toUpperCase() + "@acme.AFRICA"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM waitlist_signups WHERE lower(email) = ?", Long.class, local + "@acme.africa")).isZero();
    }
}
