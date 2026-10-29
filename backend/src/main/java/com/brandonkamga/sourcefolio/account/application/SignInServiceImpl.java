package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.AuthIdentity;
import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.AuthIdentityStore;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;

import java.time.Clock;
import java.time.Instant;

public class SignInServiceImpl implements SignInService {

    private final UserStore users;
    private final AuthIdentityStore identities;
    private final Clock clock;

    public SignInServiceImpl(UserStore users, AuthIdentityStore identities, Clock clock) {
        this.users = users;
        this.identities = identities;
        this.clock = clock;
    }

    @Override
    public Member signIn(ExternalIdentity external) {
        Instant now = clock.instant();
        return identities.find(external.provider(), external.providerUserId())
                .map(identity -> returning(identity, external.login(), now))
                .orElseGet(() -> registering(external, now));
    }

    @Override
    public Member requireMember(UserId id) {
        User user = users.find(id).orElseThrow(() -> new NotFoundException("account.member-not-found"));
        AuthIdentity identity = identities.findByUser(id, AuthProvider.GITHUB)
                .orElseThrow(() -> new NotFoundException("account.member-not-found"));
        return new Member(user.id(), identity.login(), user.language());
    }

    // A returning member keeps their account and their language; only a GitHub rename is followed.
    private Member returning(AuthIdentity identity, String login, Instant now) {
        AuthIdentity current = identity.hasLogin(login) ? identity : identities.save(identity.renamedTo(login, now));
        User user = users.find(current.userId())
                .orElseThrow(() -> new IllegalStateException("Identity " + current.id() + " points to no user"));
        return new Member(user.id(), current.login(), user.language());
    }

    private Member registering(ExternalIdentity external, Instant now) {
        User user = users.save(User.register(external.language(), now));
        AuthIdentity identity = identities.save(
                AuthIdentity.link(user.id(), external.provider(), external.providerUserId(), external.login(), now));
        return new Member(user.id(), identity.login(), user.language());
    }
}
