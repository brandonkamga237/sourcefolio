package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackCursor;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** A copying fake of the feedback table, newest first like the SQL. */
class InMemoryFeedbackStore implements FeedbackStore {

    final List<Feedback> rows = new ArrayList<>();

    private static final Comparator<Feedback> NEWEST_FIRST =
            Comparator.comparing(Feedback::createdAt).thenComparing(Feedback::id).reversed();

    @Override
    public void add(Feedback feedback) {
        rows.add(feedback);
    }

    @Override
    public List<Feedback> page(UUID showcaseId, FeedbackCursor after, int limit) {
        return rows.stream()
                .filter(row -> row.showcaseId().equals(showcaseId))
                .filter(row -> after == null || row.createdAt().isBefore(after.createdAt())
                        || row.createdAt().equals(after.createdAt()) && row.id().compareTo(after.id()) < 0)
                .sorted(NEWEST_FIRST).limit(limit).toList();
    }

    @Override
    public Optional<Feedback> find(UUID id) {
        return rows.stream().filter(row -> row.id().equals(id)).findFirst();
    }

    @Override
    public void delete(UUID id) {
        rows.removeIf(row -> row.id().equals(id));
    }

    @Override
    public int count(UUID showcaseId) {
        return (int) rows.stream().filter(row -> row.showcaseId().equals(showcaseId)).count();
    }

    @Override
    public int countSince(UUID showcaseId, Instant after) {
        return (int) rows.stream().filter(row -> row.showcaseId().equals(showcaseId))
                .filter(row -> after == null || row.createdAt().isAfter(after)).count();
    }

    @Override
    public List<UUID> removeAllBy(UUID authorId) {
        List<UUID> showcases = rows.stream().filter(row -> row.authorId().equals(authorId)).map(Feedback::showcaseId).distinct().toList();
        rows.removeIf(row -> row.authorId().equals(authorId));
        return showcases;
    }

    @Override
    public void renameAuthor(UUID authorId, String login) {
        rows.replaceAll(row -> row.authorId().equals(authorId)
                ? new Feedback(row.id(), row.showcaseId(), authorId, login, row.angle(), row.body(), row.language(), row.createdAt())
                : row);
    }
}
