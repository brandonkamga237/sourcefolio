package com.brandonkamga.sourcefolio.waitlist.infra.persistence;

import com.brandonkamga.sourcefolio.shared.domain.EmailAddress;
import com.brandonkamga.sourcefolio.waitlist.domain.port.WaitlistStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
class JpaWaitlistStore implements WaitlistStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void add(EmailAddress email, Instant consentedAt) {
        entityManager.createNativeQuery("""
                        INSERT INTO waitlist_signups (email, consented_at)
                        VALUES (:email, :consentedAt)
                        ON CONFLICT (lower(email)) DO NOTHING""")
                .setParameter("email", email.value())
                .setParameter("consentedAt", consentedAt)
                .executeUpdate();
    }
}
