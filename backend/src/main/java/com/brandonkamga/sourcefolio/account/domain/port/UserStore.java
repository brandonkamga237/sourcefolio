package com.brandonkamga.sourcefolio.account.domain.port;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;

import java.util.Optional;

public interface UserStore {

    User save(User user);

    Optional<User> find(UserId id);
}
