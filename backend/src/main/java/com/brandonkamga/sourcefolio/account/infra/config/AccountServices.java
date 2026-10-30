package com.brandonkamga.sourcefolio.account.infra.config;

import com.brandonkamga.sourcefolio.account.application.AccountSettings;
import com.brandonkamga.sourcefolio.account.application.AccountSettingsImpl;
import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.application.SignInServiceImpl;
import com.brandonkamga.sourcefolio.account.application.AccountEvents;
import com.brandonkamga.sourcefolio.account.domain.port.AuthIdentityStore;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class AccountServices {

    @Bean
    SignInService signInService(UserStore users, AuthIdentityStore identities, AccountEvents events, Clock clock,
                                TransactionalServices transactional) {
        return transactional.wrap(SignInService.class, new SignInServiceImpl(users, identities, events, clock));
    }

    @Bean
    AccountSettings accountSettings(UserStore users, TransactionalServices transactional) {
        return transactional.wrap(AccountSettings.class, new AccountSettingsImpl(users));
    }
}
