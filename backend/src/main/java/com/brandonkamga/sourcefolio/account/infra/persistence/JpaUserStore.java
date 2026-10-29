package com.brandonkamga.sourcefolio.account.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class JpaUserStore implements UserStore {

    private final UserJpaRepository repository;

    JpaUserStore(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(new UserEntity(user.id().value(), user.language().tag(), user.email(), user.createdAt())));
    }

    @Override
    public Optional<User> find(UserId id) {
        return repository.findById(id.value()).map(JpaUserStore::toDomain);
    }

    private static User toDomain(UserEntity entity) {
        return new User(new UserId(entity.getId()), Language.fromTag(entity.getLanguage()), entity.getEmail(), entity.getCreatedAt());
    }
}
