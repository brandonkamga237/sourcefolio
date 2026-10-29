package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignInServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-29T10:00:00Z");

    private final InMemoryUserStore users = new InMemoryUserStore();
    private final InMemoryAuthIdentityStore identities = new InMemoryAuthIdentityStore();
    private final SignInService signIn = new SignInServiceImpl(users, identities, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("a first sign-in registers the member in their browser's language and links the GitHub account")
    void firstSignInRegisters() {
        Member member = signIn.signIn(github("42", "nekwalla", Language.FR));

        assertThat(users.rows).hasSize(1);
        assertThat(users.rows.get(member.id()).language()).isEqualTo(Language.FR);
        assertThat(users.rows.get(member.id()).createdAt()).isEqualTo(NOW);
        assertThat(identities.rows.values()).singleElement().satisfies(identity -> {
            assertThat(identity.userId()).isEqualTo(member.id());
            assertThat(identity.providerUserId()).isEqualTo("42");
            assertThat(identity.login()).isEqualTo("nekwalla");
        });
    }

    @Test
    @DisplayName("signing in again finds the same member, without a second account and without changing their language")
    void returningMemberIsFound() {
        Member first = signIn.signIn(github("42", "nekwalla", Language.FR));

        Member again = signIn.signIn(github("42", "nekwalla", Language.EN));

        assertThat(again.id()).isEqualTo(first.id());
        assertThat(again.language()).isEqualTo(Language.FR);
        assertThat(users.rows).hasSize(1);
        assertThat(identities.rows).hasSize(1);
    }

    @Test
    @DisplayName("a GitHub rename is followed: same member, new login stored")
    void renameIsFollowed() {
        Member first = signIn.signIn(github("42", "nekwalla", Language.FR));
        Instant later = NOW.plusSeconds(3600);
        SignInService laterSignIn = new SignInServiceImpl(users, identities, Clock.fixed(later, ZoneOffset.UTC));

        Member renamed = laterSignIn.signIn(github("42", "nadege-ekwalla", Language.FR));

        assertThat(renamed.id()).isEqualTo(first.id());
        assertThat(renamed.login()).isEqualTo("nadege-ekwalla");
        assertThat(identities.rows.values()).singleElement().satisfies(identity -> {
            assertThat(identity.login()).isEqualTo("nadege-ekwalla");
            assertThat(identity.updatedAt()).isEqualTo(later);
            assertThat(identity.createdAt()).isEqualTo(NOW);
        });
    }

    @Test
    @DisplayName("two GitHub accounts are two members, even with the same login at different times")
    void identityIsTheProviderIdNotTheLogin() {
        Member first = signIn.signIn(github("42", "dev", Language.EN));
        Member second = signIn.signIn(github("43", "dev", Language.EN));

        assertThat(second.id()).isNotEqualTo(first.id());
    }

    @Test
    @DisplayName("the current member is read back with their login and language")
    void requireMember() {
        Member member = signIn.signIn(github("42", "nekwalla", Language.FR));

        assertThat(signIn.requireMember(member.id())).isEqualTo(member);
    }

    @Test
    @DisplayName("an unknown member is a not-found, not a crash")
    void unknownMember() {
        assertThatThrownBy(() -> signIn.requireMember(UserId.newId())).isInstanceOf(NotFoundException.class);
    }

    private static ExternalIdentity github(String id, String login, Language language) {
        return new ExternalIdentity(AuthProvider.GITHUB, id, login, language);
    }
}
