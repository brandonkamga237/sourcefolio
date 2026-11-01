package com.brandonkamga.sourcefolio.engagement.infra.persistence;

import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackAngle;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackBody;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackCursor;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

@Component
class JpaFeedbackStore implements FeedbackStore {

    private final FeedbackJpaRepository feedback;

    JpaFeedbackStore(FeedbackJpaRepository feedback) {
        this.feedback = feedback;
    }

    @Override
    public void add(Feedback item) {
        feedback.save(new FeedbackEntity(item.id(), item.showcaseId(), item.authorId(), item.authorLogin(),
                item.angle() == null ? null : item.angle().name(), item.body().value(), item.language().tag(), item.createdAt()));
    }

    @Override
    public List<Feedback> page(UUID showcaseId, FeedbackCursor after, int limit) {
        List<FeedbackEntity> rows = after == null
                ? feedback.firstPage(showcaseId, Limit.of(limit))
                : feedback.pageAfter(showcaseId, after.createdAt(), after.id(), Limit.of(limit));
        return rows.stream().map(JpaFeedbackStore::toDomain).toList();
    }

    @Override
    public Optional<Feedback> find(UUID id) {
        return feedback.findById(id).map(JpaFeedbackStore::toDomain);
    }

    @Override
    public void delete(UUID id) {
        feedback.deleteById(id);
    }

    @Override
    public int count(UUID showcaseId) {
        return feedback.countVisible(showcaseId);
    }

    @Override
    public int countSince(UUID showcaseId, Instant after) {
        return after == null ? feedback.countVisible(showcaseId) : feedback.countVisibleAfter(showcaseId, after);
    }

    @Override
    public void renameAuthor(UUID authorId, String login) {
        feedback.renameAuthor(authorId, login);
    }

    @Override
    public List<UUID> removeAllBy(UUID authorId) {
        List<UUID> showcases = feedback.showcasesFeedbackBy(authorId);
        feedback.deleteAllBy(authorId);
        return showcases;
    }

    private static Feedback toDomain(FeedbackEntity row) {
        return new Feedback(row.getId(), row.getShowcaseId(), row.getAuthorId(), row.getAuthorLogin(),
                row.getAngle() == null ? null : FeedbackAngle.valueOf(row.getAngle()), new FeedbackBody(row.getBody()),
                Language.fromTag(row.getLanguage()), row.getCreatedAt());
    }
}
