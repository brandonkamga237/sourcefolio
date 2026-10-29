package com.brandonkamga.sourcefolio.account.domain.port;

import com.brandonkamga.sourcefolio.account.domain.model.AuthIdentity;
import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;

import java.util.Optional;

public interface AuthIdentityStore {

    AuthIdentity save(AuthIdentity identity);

    Optional<AuthIdentity> find(AuthProvider provider, String providerUserId);

    Optional<AuthIdentity> findByUser(UserId userId, AuthProvider provider);
}
