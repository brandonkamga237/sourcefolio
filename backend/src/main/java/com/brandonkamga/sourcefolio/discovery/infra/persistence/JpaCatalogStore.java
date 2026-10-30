package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
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
    @Transactional(readOnly = true)
    public List<RankedEntry> ranking(int limit) {
        return entries.mostLiked(Limit.of(limit)).stream().map(row -> new RankedEntry(toDomain(row), row.getLikeCount())).toList();
    }

    // Two queries whatever the page size: the ordered ids and keys, then their rows.
    @Override
    @Transactional(readOnly = true)
    public List<Shuffled> shuffled(LocalDate day, boolean young, Instant youngSince, String afterKey, int limit) {
        List<String[]> idsAndKeys = entries.shuffledIdsAndKeys(day.toString(), young, youngSince, afterKey, limit).stream()
                .map(row -> row.split(":", 2))
                .toList();
        Map<UUID, CatalogEntryEntity> rows = entries.findAllById(idsAndKeys.stream().map(row -> UUID.fromString(row[0])).toList())
                .stream().collect(Collectors.toMap(CatalogEntryEntity::getShowcaseId, Function.identity()));
        return idsAndKeys.stream()
                .filter(row -> rows.containsKey(UUID.fromString(row[0])))
                .map(row -> new Shuffled(toDomain(rows.get(UUID.fromString(row[0]))), row[1]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return entries.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogEntry> recent(RecentCursor after, int limit) {
        List<CatalogEntryEntity> rows = after == null
                ? entries.findAllByOrderByPublishedAtDescShowcaseIdDesc(Limit.of(limit))
                : entries.recentAfter(after.publishedAt(), after.showcaseId(), Limit.of(limit));
        return rows.stream().map(JpaCatalogStore::toDomain).toList();
    }

    private static CatalogEntry toDomain(CatalogEntryEntity row) {
        return new CatalogEntry(row.getShowcaseId(), row.getSlug(), row.getSiteUrl(), row.getRepository(),
                row.getRepositoryUrl(), row.getOwnerUserId(), row.getOwnerLogin(), row.getPublishedAt(),
                row.getPreviewImageUrl(), row.getPreviewVideoUrl());
    }
}
