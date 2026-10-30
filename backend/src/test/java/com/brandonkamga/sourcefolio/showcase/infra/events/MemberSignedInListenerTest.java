package com.brandonkamga.sourcefolio.showcase.infra.events;

import com.brandonkamga.sourcefolio.account.application.MemberSignedIn;
import com.brandonkamga.sourcefolio.showcase.application.ShowcaseDetection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MemberSignedInListenerTest {

    @Test
    @DisplayName("a sign-in asks for a fresh detection of that member's portfolio")
    void signInRequestsDetection() {
        ShowcaseDetection detection = mock(ShowcaseDetection.class);
        UUID member = UUID.randomUUID();

        new MemberSignedInListener(detection).on(new MemberSignedIn(member, "nekwalla"));

        verify(detection).requestDetection(member, "nekwalla");
    }
}
