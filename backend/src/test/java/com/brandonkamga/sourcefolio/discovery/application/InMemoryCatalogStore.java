package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankedEntry;
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
    final Map<UUID, Integer> likes = new LinkedHashMap<>();

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
    public List<Shuffled> shuffled(LocalDate day, boolean young, Instant youngSince, String afterKey, int limit, CatalogFilter filter) {
        return rows.values().stream()
                .filter(entry -> matches(entry, filter))
                .filter(entry -> !entry.publishedAt().isBefore(youngSince) == young)
                .map(entry -> new Shuffled(entry, md5(entry.showcaseId() + day.toString())))
                .filter(shuffled -> shuffled.key().compareTo(afterKey) > 0)
                .sorted(Comparator.comparing(Shuffled::key))
                .limit(limit)
                .toList();
    }

    @Override
    public void setPreview(UUID showcaseId, String imageUrl, String videoUrl) {
        rows.computeIfPresent(showcaseId, (id, entry) -> new CatalogEntry(entry.showcaseId(), entry.slug(), entry.siteUrl(),
                entry.repository(), entry.repositoryUrl(), entry.ownerId(), entry.ownerLogin(), entry.publishedAt(), imageUrl, videoUrl));
    }

    @Override
    public void setLikes(UUID showcaseId, int count) {
        likes.put(showcaseId, count);
    }

    @Override
    public List<CatalogEntry> byOwnerLogin(String login) {
        return rows.values().stream().filter(entry -> entry.ownerLogin().equalsIgnoreCase(login))
                .sorted(java.util.Comparator.comparing(CatalogEntry::publishedAt).reversed()).toList();
    }

    @Override
    public void setAuthorProfile(UUID ownerId, String countryCode, List<String> specialties, String headline) {
        rows.replaceAll((id, entry) -> entry.ownerId().equals(ownerId)
                ? new CatalogEntry(entry.showcaseId(), entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(), entry.ownerId(),
                        entry.ownerLogin(), entry.publishedAt(), entry.previewImageUrl(), entry.previewVideoUrl(), entry.repositoryStars(),
                        entry.likes(), countryCode, specialties, headline)
                : entry);
    }

    @Override
    public void setRepositoryStars(UUID showcaseId, Integer stars) {
        CatalogEntry entry = rows.get(showcaseId);
        if (entry != null) {
            rows.put(showcaseId, new CatalogEntry(entry.showcaseId(), entry.slug(), entry.siteUrl(), entry.repository(), entry.repositoryUrl(),
                    entry.ownerId(), entry.ownerLogin(), entry.publishedAt(), entry.previewImageUrl(), entry.previewVideoUrl(), stars));
        }
    }

    @Override
    public List<RankedEntry> ranking(int limit) {
        return rows.values().stream()
                .filter(entry -> likes.getOrDefault(entry.showcaseId(), 0) > 0)
                .sorted(Comparator.comparing((CatalogEntry entry) -> likes.get(entry.showcaseId())).reversed()
                        .thenComparing(Comparator.comparing(CatalogEntry::publishedAt).reversed()))
                .limit(limit)
                .map(entry -> new RankedEntry(entry, likes.get(entry.showcaseId())))
                .toList();
    }

    @Override
    public long count(CatalogFilter filter) {
        return rows.values().stream().filter(entry -> matches(entry, filter)).count();
    }

    @Override
    public List<CatalogEntry> recent(RecentCursor after, int limit, CatalogFilter filter) {
        Comparator<CatalogEntry> newestFirst = Comparator.comparing(CatalogEntry::publishedAt)
                .thenComparing(CatalogEntry::showcaseId).reversed();
        return rows.values().stream()
                .filter(entry -> matches(entry, filter))
                .filter(entry -> after == null || entry.publishedAt().isBefore(after.publishedAt())
                        || entry.publishedAt().equals(after.publishedAt()) && entry.showcaseId().compareTo(after.showcaseId()) < 0)
                .sorted(newestFirst)
                .limit(limit)
                .toList();
    }

    @Override
    public CatalogFacets facets() {
        java.util.function.Function<java.util.stream.Stream<String>, List<CatalogFacets.Facet>> count = values -> values
                .collect(java.util.stream.Collectors.groupingBy(value -> value, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new CatalogFacets.Facet(entry.getKey(), entry.getValue()))
                .toList();
        return new CatalogFacets(count.apply(rows.values().stream().map(CatalogEntry::countryCode).filter(java.util.Objects::nonNull)),
                count.apply(rows.values().stream().flatMap(entry -> entry.specialties().stream())));
    }

    // The same rules as the SQL filters: any part left null matches everything.
    private static boolean matches(CatalogEntry entry, CatalogFilter filter) {
        String haystack = (entry.ownerLogin() + " " + entry.siteUrl() + " " + (entry.headline() == null ? "" : entry.headline())).toLowerCase();
        return (filter.countryCode() == null || filter.countryCode().equals(entry.countryCode()))
                && (filter.specialty() == null || entry.specialties().contains(filter.specialty()))
                && (filter.text() == null || haystack.contains(filter.text()));
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
