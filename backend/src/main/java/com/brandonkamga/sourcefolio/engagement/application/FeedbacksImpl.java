package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackAngle;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackBody;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackCursor;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.exception.ForbiddenException;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcase;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcases;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public class FeedbacksImpl implements Feedbacks {

    private final PublicShowcases showcases;
    private final FeedbackStore feedback;
    private final SignInService members;
    private final Clock clock;

    public FeedbacksImpl(PublicShowcases showcases, FeedbackStore feedback, SignInService members, Clock clock) {
        this.showcases = showcases;
        this.feedback = feedback;
        this.members = members;
        this.clock = clock;
    }

    // One more than asked, to know whether a next page exists without counting.
    @Override
    public FeedbackPage list(String slug, String cursor, int size) {
        UUID showcaseId = require(slug).showcase().id().value();
        int limit = Math.clamp(size, 1, MAX_PAGE_SIZE);
        List<Feedback> rows = feedback.page(showcaseId, FeedbackCursor.decode(cursor).orElse(null), limit + 1);
        int total = feedback.count(showcaseId);
        if (rows.size() <= limit) {
            return new FeedbackPage(rows, null, total);
        }
        List<Feedback> page = rows.subList(0, limit);
        Feedback last = page.getLast();
        return new FeedbackPage(page, new FeedbackCursor(last.createdAt(), last.id()).encode(), total);
    }

    @Override
    public Feedback post(UUID memberId, String slug, String angle, String body, Language language) {
        PublicShowcase found = require(slug);
        if (found.showcase().ownerId().equals(memberId)) {
            throw new ForbiddenException("feedback.own-showcase");
        }
        FeedbackAngle parsedAngle = null;
        if (angle != null && !angle.isBlank()) {
            parsedAngle = FeedbackAngle.parse(angle).orElseThrow(() -> new BadRequestException("feedback.angle-invalid"));
        }
        FeedbackBody parsedBody;
        try {
            parsedBody = FeedbackBody.parse(body);
        } catch (IllegalArgumentException invalid) {
            throw new BadRequestException("feedback.body-invalid");
        }
        String login = members.requireMember(new UserId(memberId)).login();
        Feedback written = Feedback.write(found.showcase().id().value(), memberId, login, parsedAngle, parsedBody, language, clock.instant());
        feedback.add(written);
        return written;
    }

    @Override
    public void delete(UUID memberId, UUID feedbackId) {
        Feedback found = feedback.find(feedbackId).orElseThrow(() -> new NotFoundException("feedback.not-found"));
        if (!found.authorId().equals(memberId)) {
            throw new ForbiddenException("feedback.not-yours");
        }
        feedback.delete(feedbackId);
    }

    @Override
    public void followRename(UUID memberId, String login) {
        feedback.renameAuthor(memberId, login);
    }

    private PublicShowcase require(String slug) {
        return showcases.findBySlug(slug).orElseThrow(() -> new NotFoundException("showcase.not-found"));
    }
}
