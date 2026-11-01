package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankedEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.RecentCursor;
import com.brandonkamga.sourcefolio.discovery.domain.model.Shuffled;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaCatalogStore implements CatalogStore {

    private final CatalogEntryJpaRepository entries;

    JpaCatalogStore(CatalogEntryJpaRepository entries) {
        this.entries = entries;
    }

    // Writes come from event listeners that run after the publishing transaction committed. A
    // write joining that finished transaction would never be committed, so each opens its own.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsert(CatalogEntry entry) {
        entries.upsert(entry.showcaseId(), entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(),
                entry.ownerId(), entry.ownerLogin(), entry.publishedAt());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void renameOwner(UUID ownerId, String login) {
        entries.renameOwner(ownerId, login);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setPreview(UUID showcaseId, String imageUrl, String videoUrl) {
        entries.setPreview(showcaseId, imageUrl, videoUrl);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setLikes(UUID showcaseId, int count) {
        entries.setLikes(showcaseId, count);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setFeedbackCount(UUID showcaseId, int count) {
        entries.setFeedbackCount(showcaseId, count);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setRepository(UUID showcaseId, Integer stars, String description, List<String> languages, Instant pushedAt) {
        entries.setRepository(showcaseId, stars, description, languages.toArray(String[]::new), pushedAt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogEntry> byOwnerLogin(String login) {
        return entries.byOwnerLogin(login).stream().map(JpaCatalogStore::toDomain).toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setAuthorProfile(UUID ownerId, String countryCode, List<String> specialties, String headline, String role) {
        entries.setAuthorProfile(ownerId, countryCode, specialties.toArray(String[]::new), headline, role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankedEntry> ranking(int limit) {
        return entries.mostLiked(Limit.of(limit)).stream().map(row -> new RankedEntry(toDomain(row), row.getLikeCount())).toList();
    }

    // Two queries whatever the page size: the ordered ids and keys, then their rows.
    @Override
    @Transactional(readOnly = true)
    public List<Shuffled> shuffled(LocalDate day, boolean young, Instant youngSince, String afterKey, int limit, CatalogFilter filter) {
        List<String[]> idsAndKeys = entries.shuffledIdsAndKeys(day.toString(), young, youngSince, afterKey, limit,
                        filter.countryCode(), filter.language(), filter.role(), pattern(filter)).stream()
                .map(row -> row.split(":", 2))
                .toList();
        Map<UUID, CatalogEntryEntity> rows = rowsById(idsAndKeys.stream().map(row -> UUID.fromString(row[0])).toList());
        return idsAndKeys.stream()
                .filter(row -> rows.containsKey(UUID.fromString(row[0])))
                .map(row -> new Shuffled(toDomain(rows.get(UUID.fromString(row[0]))), row[1]))
                .toList();
    }

    // Three grouped queries, whatever the size of the catalogue: PostgreSQL counts.
    @Override
    @Transactional(readOnly = true)
    public CatalogFacets facets() {
        return new CatalogFacets(
                entries.countryFacets().stream().map(row -> new CatalogFacets.Facet(row.getValue().strip(), row.getCount())).toList(),
                entries.languageFacets().stream().map(row -> new CatalogFacets.Facet(row.getValue(), row.getCount())).toList(),
                entries.roleFacets().stream().map(row -> new CatalogFacets.Facet(row.getValue(), row.getCount())).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long count(CatalogFilter filter) {
        return filter.isEmpty() ? entries.count() : entries.countMatching(filter.countryCode(), filter.language(), filter.role(), pattern(filter));
    }

    // Two queries as well: the ordered ids, then their rows.
    @Override
    @Transactional(readOnly = true)
    public List<CatalogEntry> recent(RecentCursor after, int limit, CatalogFilter filter) {
        List<UUID> ids = entries.recentIds(after == null ? null : after.publishedAt(), after == null ? null : after.showcaseId(), limit,
                filter.countryCode(), filter.language(), filter.role(), pattern(filter));
        Map<UUID, CatalogEntryEntity> rows = rowsById(ids);
        return ids.stream().filter(rows::containsKey).map(id -> toDomain(rows.get(id))).toList();
    }

    private Map<UUID, CatalogEntryEntity> rowsById(List<UUID> ids) {
        return entries.findAllById(ids).stream().collect(Collectors.toMap(CatalogEntryEntity::getShowcaseId, Function.identity()));
    }

    // "%words%" for LIKE, with the visitor's own % and _ taken literally.
    private static String pattern(CatalogFilter filter) {
        if (filter.text() == null) {
            return null;
        }
        return "%" + filter.text().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    private static CatalogEntry toDomain(CatalogEntryEntity row) {
        return new CatalogEntry(row.getShowcaseId(), row.getSlug(), row.getSiteUrl(), row.getRepository(),
                row.getRepositoryUrl(), row.getOwnerUserId(), row.getOwnerLogin(), row.getPublishedAt(),
                row.getPreviewImageUrl(), row.getPreviewVideoUrl(), row.getRepositoryStars(), row.getLikeCount(),
                row.getCountryCode(), List.of(row.getSpecialties()), row.getHeadline(), row.getRepositoryDescription(),
                List.of(row.getRepositoryLanguages()), row.getRepositoryPushedAt(), row.getAuthorRole(), row.getFeedbackCount());
    }
}
