package com.brandonkamga.sourcefolio.account.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.AuthIdentity;
import com.brandonkamga.sourcefolio.account.domain.model.AuthProvider;
import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.AuthIdentityStore;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class JpaAccountStoresIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-10-29T10:00:00Z");

    @Autowired
    private UserStore users;

    @Autowired
    private AuthIdentityStore identities;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("a user and their GitHub identity are stored and read back field for field")
    void roundTrip() {
        User user = users.save(User.register(Language.FR, NOW));
        AuthIdentity identity = identities.save(AuthIdentity.link(user.id(), AuthProvider.GITHUB, "42", "nekwalla", NOW));
        entityManager.flush();
        entityManager.clear();

        assertThat(users.find(user.id())).contains(user);
        assertThat(identities.find(AuthProvider.GITHUB, "42")).contains(identity);
        assertThat(identities.findByUser(user.id(), AuthProvider.GITHUB)).contains(identity);
    }

    @Test
    @DisplayName("deleting a user removes the row, and the database removes their GitHub identity with it")
    void deleteCascades() {
        User user = users.save(User.register(Language.FR, NOW));
        identities.save(AuthIdentity.link(user.id(), AuthProvider.GITHUB, "77", "leaving", NOW));
        entityManager.flush();
        entityManager.clear();

        users.delete(user.id());
        entityManager.clear();

        assertThat(users.find(user.id())).isEmpty();
        assertThat(identities.find(AuthProvider.GITHUB, "77")).isEmpty();
    }

    @Test
    @DisplayName("a rename updates the stored login and its update time")
    void renameIsStored() {
        User user = users.save(User.register(Language.EN, NOW));
        AuthIdentity identity = identities.save(AuthIdentity.link(user.id(), AuthProvider.GITHUB, "42", "old", NOW));
        Instant later = NOW.plus(1, ChronoUnit.DAYS);

        identities.save(identity.renamedTo("new", later));
        entityManager.flush();
        entityManager.clear();

        assertThat(identities.find(AuthProvider.GITHUB, "42")).hasValueSatisfying(stored -> {
            assertThat(stored.login()).isEqualTo("new");
            assertThat(stored.updatedAt()).isEqualTo(later);
        });
    }

    @Test
    @DisplayName("the same GitHub account cannot be linked twice")
    void providerAccountIsUnique() {
        User first = users.save(User.register(Language.EN, NOW));
        User second = users.save(User.register(Language.EN, NOW));
        identities.save(AuthIdentity.link(first.id(), AuthProvider.GITHUB, "42", "dev", NOW));
        entityManager.flush();

        assertThatThrownBy(() -> {
            identities.save(AuthIdentity.link(second.id(), AuthProvider.GITHUB, "42", "dev", NOW));
            entityManager.flush();
        }).isInstanceOfAny(DataIntegrityViolationException.class, org.hibernate.exception.ConstraintViolationException.class);
    }
}
