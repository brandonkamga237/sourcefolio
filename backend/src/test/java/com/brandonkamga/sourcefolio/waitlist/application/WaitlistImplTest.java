package com.brandonkamga.sourcefolio.waitlist.application;

import com.brandonkamga.sourcefolio.shared.domain.EmailAddress;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitlistImplTest {

    private static final Instant NOW = Instant.parse("2026-11-08T10:00:00Z");

    private final List<EmailAddress> stored = new ArrayList<>();
    private final List<Instant> dates = new ArrayList<>();
    private final Waitlist waitlist = new WaitlistImpl((email, consentedAt) -> {
        stored.add(email);
        dates.add(consentedAt);
    }, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("a work address with consent is stored with the date of the consent")
    void stores() {
        waitlist.join(new JoinWaitlist("  ada@acme.africa ", true));

        assertThat(stored).containsExactly(new EmailAddress("ada@acme.africa"));
        assertThat(dates).containsExactly(NOW);
    }

    @Test
    @DisplayName("nothing is stored without consent")
    void needsConsent() {
        assertThatThrownBy(() -> waitlist.join(new JoinWaitlist("ada@acme.africa", false))).isInstanceOf(BadRequestException.class);
        assertThat(stored).isEmpty();
    }

    @Test
    @DisplayName("an address of the wrong shape, or none, is refused")
    void refusesBadAddresses() {
        assertThatThrownBy(() -> waitlist.join(new JoinWaitlist("not-an-address", true))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> waitlist.join(new JoinWaitlist(null, true))).isInstanceOf(BadRequestException.class);
        assertThat(stored).isEmpty();
    }
}
