package com.brandonkamga.sourcefolio.showcase.infra.account;

import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.showcase.domain.port.MemberDirectory;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Members through the account module's published interface, never its tables. */
@Component
class AccountMemberDirectory implements MemberDirectory {

    private final SignInService accounts;

    AccountMemberDirectory(SignInService accounts) {
        this.accounts = accounts;
    }

    @Override
    public String loginOf(UUID memberId) {
        return accounts.requireMember(new UserId(memberId)).login();
    }
}
