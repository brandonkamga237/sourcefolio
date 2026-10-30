package com.brandonkamga.sourcefolio.account.application;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Records are immutable, so storing them is already a copy: a forgotten save shows up as missing state.
class InMemoryUserStore implements UserStore {

    final Map<UserId, User> rows = new HashMap<>();

    @Override
    public User save(User user) {
        rows.put(user.id(), user);
        return user;
    }

    @Override
    public Optional<User> find(UserId id) {
        return Optional.ofNullable(rows.get(id));
    }
}
