package com.brandonkamga.sourcefolio.checkup.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.OptionalInt;
import static org.assertj.core.api.Assertions.assertThat;

class SiteReachabilityImplTest {

    @Test
    @DisplayName("a site answers when its final page is a 2xx")
    void successAnswers() {
        assertThat(new SiteReachabilityImpl(url -> OptionalInt.of(200)).isReachable("https://nekwalla.dev/")).isTrue();
    }

    @Test
    @DisplayName("an error page or no answer at all is not a site that answers")
    void errorsDoNotAnswer() {
        assertThat(new SiteReachabilityImpl(url -> OptionalInt.of(404)).isReachable("https://nekwalla.dev/")).isFalse();
        assertThat(new SiteReachabilityImpl(url -> OptionalInt.of(503)).isReachable("https://nekwalla.dev/")).isFalse();
        assertThat(new SiteReachabilityImpl(url -> OptionalInt.empty()).isReachable("https://nekwalla.dev/")).isFalse();
    }
}
