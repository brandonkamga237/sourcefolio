package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.RecentCursor;
import com.brandonkamga.sourcefolio.discovery.domain.model.Shuffled;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Same semantics as the PostgreSQL adapter, shuffle key included (md5 of id and day). */
class InMemoryCatalogStore implements CatalogStore {

    final Map<UUID, CatalogEntry> rows = new LinkedHashMap<>();

    @Override
    public void upsert(CatalogEntry entry) {
        rows.put(entry.showcaseId(), entry);
    }

    @Override
    public void renameOwner(UUID ownerId, String login) {
        rows.replaceAll((id, entry) -> entry.ownerId().equals(ownerId)
                ? new CatalogEntry(entry.showcaseId(), entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(),
                entry.ownerId(), login, entry.publishedAt())
                : entry);
    }

    @Override
    public List<Shuffled> shuffled(LocalDate day, boolean young, Instant youngSince, String afterKey, int limit) {
        return rows.values().stream()
                .filter(entry -> !entry.publishedAt().isBefore(youngSince) == young)
                .map(entry -> new Shuffled(entry, md5(entry.showcaseId() + day.toString())))
                .filter(shuffled -> shuffled.key().compareTo(afterKey) > 0)
                .sorted(Comparator.comparing(Shuffled::key))
                .limit(limit)
                .toList();
    }

    @Override
    public List<CatalogEntry> recent(RecentCursor after, int limit) {
        Comparator<CatalogEntry> newestFirst = Comparator.comparing(CatalogEntry::publishedAt)
                .thenComparing(CatalogEntry::showcaseId).reversed();
        return rows.values().stream()
                .filter(entry -> after == null || entry.publishedAt().isBefore(after.publishedAt())
                        || entry.publishedAt().equals(after.publishedAt()) && entry.showcaseId().compareTo(after.showcaseId()) < 0)
                .sorted(newestFirst)
                .limit(limit)
                .toList();
    }

    private static String md5(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
