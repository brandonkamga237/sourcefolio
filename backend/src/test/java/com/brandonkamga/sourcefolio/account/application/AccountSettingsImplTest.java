package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountSettingsImplTest {

    private final InMemoryUserStore users = new InMemoryUserStore();
    private final AccountSettings settings = new AccountSettingsImpl(users);
    private final UserId member = users.save(User.register(Language.FR, Instant.parse("2026-10-30T09:00:00Z"))).id();

    @Test
    @DisplayName("a member has no alert address until they give one, then it is stored")
    void setEmail() {
        assertThat(settings.findEmail(member)).isEmpty();

        settings.changeEmail(new ChangeEmail(member, " ayo@example.cm "));

        assertThat(users.rows.get(member).email()).isEqualTo("ayo@example.cm");
        assertThat(settings.findEmail(member)).contains("ayo@example.cm");
    }

    @Test
    @DisplayName("an empty answer removes the address")
    void removeEmail() {
        settings.changeEmail(new ChangeEmail(member, "ayo@example.cm"));

        settings.changeEmail(new ChangeEmail(member, "  "));

        assertThat(users.rows.get(member).email()).isNull();
    }

    @Test
    @DisplayName("something that is not an address is refused and the previous one kept")
    void invalidRefused() {
        settings.changeEmail(new ChangeEmail(member, "ayo@example.cm"));

        assertThatThrownBy(() -> settings.changeEmail(new ChangeEmail(member, "not-an-address")))
                .isInstanceOf(AppException.class)
                .satisfies(thrown -> {
                    assertThat(((AppException) thrown).code()).isEqualTo(ErrorCode.BAD_REQUEST);
                    assertThat(((AppException) thrown).messageKey()).isEqualTo("account.email-invalid");
                });
        assertThat(users.rows.get(member).email()).isEqualTo("ayo@example.cm");
    }
}
