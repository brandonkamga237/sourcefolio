package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaShowcaseStore implements ShowcaseStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<String> findMediaKeysByOwner(UUID ownerId) {
        return entityManager.createNativeQuery("""
                        SELECT media_key
                        FROM showcases
                        CROSS JOIN LATERAL unnest(ARRAY[preview_image_key, preview_video_key]) AS media_key
                        WHERE owner_user_id = :owner AND media_key IS NOT NULL""")
                .setParameter("owner", ownerId)
                .getResultList();
    }
}
