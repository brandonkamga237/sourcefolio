package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.EmailAddress;
import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import java.util.Optional;

public class AccountSettingsImpl implements AccountSettings {

    private final UserStore users;

    public AccountSettingsImpl(UserStore users) {
        this.users = users;
    }

    @Override
    public Optional<String> findEmail(UserId memberId) {
        return Optional.ofNullable(require(memberId).email());
    }

    @Override
    public Optional<String> changeEmail(ChangeEmail command) {
        User user = require(command.memberId());
        EmailAddress email = null;
        if (command.email() != null && !command.email().isBlank()) {
            email = EmailAddress.parse(command.email()).orElseThrow(() -> new BadRequestException("account.email-invalid"));
        }
        return Optional.ofNullable(users.save(user.withEmail(email)).email());
    }

    private User require(UserId memberId) {
        return users.find(memberId).orElseThrow(() -> new NotFoundException("account.member-not-found"));
    }
}
