package com.brandonkamga.sourcefolio.showcase.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShowcaseRemovalImplTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");

    private final List<ShowcaseRemoved> announced = new ArrayList<>();

    @Test
    @DisplayName("a departing member's preview files are announced for deletion")
    void announcesTheFiles() {
        new ShowcaseRemovalImpl(owner -> List.of("previews/a.webp", "previews/b.mp4"), announced::add).removeAllOf(MEMBER);

        assertThat(announced).containsExactly(new ShowcaseRemoved(MEMBER, List.of("previews/a.webp", "previews/b.mp4")));
    }

    @Test
    @DisplayName("a member who never uploaded a preview announces nothing")
    void nothingToDelete() {
        new ShowcaseRemovalImpl(owner -> List.of(), announced::add).removeAllOf(MEMBER);

        assertThat(announced).isEmpty();
    }
}
