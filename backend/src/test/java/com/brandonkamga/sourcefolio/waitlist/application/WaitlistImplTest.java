package com.brandonkamga.sourcefolio.waitlist.application;

import com.brandonkamga.sourcefolio.shared.domain.EmailAddress;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.waitlist.domain.port.WaitlistStore;
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
    private final List<EmailAddress> removed = new ArrayList<>();
    private final Waitlist waitlist = new WaitlistImpl(new WaitlistStore() {
        @Override
        public void add(EmailAddress email, Instant consentedAt) {
            stored.add(email);
            dates.add(consentedAt);
        }

        @Override
        public void remove(EmailAddress email) {
            removed.add(email);
        }
    }, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("a work address with consent is stored with the date of the consent")
    void stores() {
        waitlist.join(new JoinWaitlist("  ada@acme.africa ", true));

        assertThat(stored).containsExactly(new EmailAddress("ada@acme.africa"));
        assertThat(dates).containsExactly(NOW);
    }

    @Test
    @DisplayName("anyone can take an address off the list; a malformed one is refused")
    void leaves() {
        waitlist.leave(" ada@acme.africa ");

        assertThat(removed).containsExactly(new EmailAddress("ada@acme.africa"));
        assertThatThrownBy(() -> waitlist.leave("nope")).isInstanceOf(BadRequestException.class);
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
