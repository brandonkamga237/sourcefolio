package com.brandonkamga.sourcefolio.profile.infra.persistence;

import com.brandonkamga.sourcefolio.account.domain.model.User;
import com.brandonkamga.sourcefolio.account.domain.port.UserStore;
import com.brandonkamga.sourcefolio.profile.domain.model.CountryCode;
import com.brandonkamga.sourcefolio.profile.domain.model.DeveloperProfile;
import com.brandonkamga.sourcefolio.profile.domain.model.Specialty;
import com.brandonkamga.sourcefolio.profile.domain.port.ProfileStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.support.PostgresIT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaProfileStoreIT extends PostgresIT {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");

    @Autowired
    private ProfileStore profiles;

    @Autowired
    private UserStore users;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("a profile is created on the first answer, updated on the next, and read back as stored")
    void roundTrip() {
        UUID member = users.save(User.register(Language.FR, NOW)).id().value();

        profiles.save(DeveloperProfile.blank(member).withCountry(new CountryCode("CM"), NOW));
        entityManager.flush();
        DeveloperProfile updated = profiles.save(profiles.find(member).orElseThrow().withSpecialty(Specialty.DATA_AI, NOW.plusSeconds(5)));
        entityManager.flush();
        entityManager.clear();

        assertThat(profiles.find(member)).contains(updated);
        assertThat(updated).isEqualTo(new DeveloperProfile(member, new CountryCode("CM"), Specialty.DATA_AI, NOW.plusSeconds(5)));
    }

    @Test
    @DisplayName("a member who never answered has no profile row")
    void none() {
        assertThat(profiles.find(users.save(User.register(Language.EN, NOW)).id().value())).isEmpty();
    }
}
