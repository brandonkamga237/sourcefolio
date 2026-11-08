package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountDeletionImplTest {

    private static final UserId MEMBER = new UserId(UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11"));

    private final InMemoryUserStore users = new InMemoryUserStore();
    private final AccountDeletion deletion = new AccountDeletionImpl(users);

    @Test
    @DisplayName("the account row goes")
    void deletes() {
        users.save(new User(MEMBER, Language.FR, "nekwalla@example.com", Instant.parse("2026-10-01T00:00:00Z")));

        deletion.delete(MEMBER);

        assertThat(users.rows).isEmpty();
    }

    @Test
    @DisplayName("an account that does not exist is a 404")
    void unknownMember() {
        assertThatThrownBy(() -> deletion.delete(MEMBER))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).code()).isEqualTo(ErrorCode.NOT_FOUND);
    }
}
