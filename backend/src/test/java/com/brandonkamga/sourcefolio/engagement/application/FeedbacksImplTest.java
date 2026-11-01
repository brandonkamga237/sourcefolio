package com.brandonkamga.sourcefolio.engagement.application;

import com.brandonkamga.sourcefolio.account.application.ExternalIdentity;
import com.brandonkamga.sourcefolio.account.application.Member;
import com.brandonkamga.sourcefolio.account.application.SignInService;
import com.brandonkamga.sourcefolio.account.domain.model.UserId;
import com.brandonkamga.sourcefolio.engagement.domain.model.Feedback;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackAngle;
import com.brandonkamga.sourcefolio.engagement.domain.model.FeedbackCursor;
import com.brandonkamga.sourcefolio.engagement.domain.port.FeedbackStore;
import com.brandonkamga.sourcefolio.shared.domain.Language;
import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbacksImplTest {

    private static final Instant NOW = Instant.parse("2026-10-30T09:00:00Z");
    private static final UUID OWNER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final UUID READER = UUID.fromString("5d1e7a40-2b9c-4c55-8f0e-6a3b2c1d0e99");
    private static final Showcase SHOWCASE = Showcase.publishVerified(OWNER, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
            new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
            CandidateReason.USER_PAGES), NOW).stored();
    private static final String BODY = "The hero loads fast, but the contact link is hard to find on a phone.";

    private final List<Feedback> rows = new ArrayList<>();
    private final FeedbackStore store = new FeedbackStore() {
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
        public void renameAuthor(UUID authorId, String login) {
            rows.replaceAll(row -> row.authorId().equals(authorId)
                    ? new Feedback(row.id(), row.showcaseId(), authorId, login, row.angle(), row.body(), row.language(), row.createdAt())
                    : row);
        }
    };
    private final SignInService members = new SignInService() {
        @Override
        public Member signIn(ExternalIdentity identity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Member requireMember(UserId id) {
            return new Member(id, id.value().equals(READER) ? "ada" : "bola", Language.FR);
        }
    };
    private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private Feedbacks feedbacks() {
        return new FeedbacksImpl(
                slug -> slug.equals("nekwalla") ? Optional.of(new PublicShowcase(SHOWCASE, "nekwalla")) : Optional.empty(),
                store, members, clock);
    }

    @Test
    @DisplayName("a member's feedback is stored under their login, with its angle, its tidied text and their language")
    void post() {
        Feedback posted = feedbacks().post(READER, "nekwalla", "mobile", "  " + BODY + "\r\n", Language.FR);

        assertThat(rows).containsExactly(posted);
        assertThat(posted.authorLogin()).isEqualTo("ada");
        assertThat(posted.angle()).isEqualTo(FeedbackAngle.MOBILE);
        assertThat(posted.body().value()).isEqualTo(BODY);
        assertThat(posted.language()).isEqualTo(Language.FR);
        assertThat(posted.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("the angle is optional")
    void noAngle() {
        assertThat(feedbacks().post(READER, "nekwalla", " ", BODY, Language.EN).angle()).isNull();
        assertThat(feedbacks().post(READER, "nekwalla", null, BODY, Language.EN).angle()).isNull();
    }

    @Test
    @DisplayName("an author cannot leave feedback on their own portfolio")
    void notOwnShowcase() {
        assertCode(() -> feedbacks().post(OWNER, "nekwalla", null, BODY, Language.EN), ErrorCode.FORBIDDEN);
        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("a text under 10 or over 1,000 characters, or an unknown angle, is refused and nothing is stored")
    void invalid() {
        assertCode(() -> feedbacks().post(READER, "nekwalla", null, "too short", Language.EN), ErrorCode.BAD_REQUEST);
        assertCode(() -> feedbacks().post(READER, "nekwalla", null, "a".repeat(1001), Language.EN), ErrorCode.BAD_REQUEST);
        assertCode(() -> feedbacks().post(READER, "nekwalla", "colours", BODY, Language.EN), ErrorCode.BAD_REQUEST);
        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("an unknown or removed showcase takes no feedback and shows none")
    void unknownShowcase() {
        assertCode(() -> feedbacks().post(READER, "nobody", null, BODY, Language.EN), ErrorCode.NOT_FOUND);
        assertCode(() -> feedbacks().list("nobody", null, 10), ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("reading page after page shows every piece of feedback once, newest first, with the total")
    void pages() {
        for (int index = 0; index < 5; index++) {
            clock = Clock.fixed(NOW.plus(Duration.ofMinutes(index)), ZoneOffset.UTC);
            feedbacks().post(READER, "nekwalla", null, BODY + " #" + index, Language.EN);
        }

        FeedbackPage first = feedbacks().list("nekwalla", null, 2);
        FeedbackPage second = feedbacks().list("nekwalla", first.nextCursor(), 2);
        FeedbackPage last = feedbacks().list("nekwalla", second.nextCursor(), 2);

        assertThat(first.total()).isEqualTo(5);
        assertThat(endings(first)).containsExactly("#4", "#3");
        assertThat(endings(second)).containsExactly("#2", "#1");
        assertThat(endings(last)).containsExactly("#0");
        assertThat(last.nextCursor()).isNull();
    }

    @Test
    @DisplayName("an unreadable cursor starts again from the newest")
    void unreadableCursor() {
        feedbacks().post(READER, "nekwalla", null, BODY, Language.EN);

        assertThat(feedbacks().list("nekwalla", "not-a-cursor", 10).items()).hasSize(1);
    }

    @Test
    @DisplayName("only the one who wrote a piece of feedback can remove it")
    void delete() {
        Feedback posted = feedbacks().post(READER, "nekwalla", null, BODY, Language.EN);
        UUID stranger = UUID.fromString("9a0e3f11-6b7c-4d2e-8f90-1a2b3c4d5e6f");

        assertCode(() -> feedbacks().delete(stranger, posted.id()), ErrorCode.FORBIDDEN);
        assertCode(() -> feedbacks().delete(OWNER, posted.id()), ErrorCode.FORBIDDEN);
        feedbacks().delete(READER, posted.id());

        assertThat(rows).isEmpty();
        assertCode(() -> feedbacks().delete(READER, posted.id()), ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("a renamed GitHub account shows its new login on the feedback it already left")
    void followRename() {
        feedbacks().post(READER, "nekwalla", null, BODY, Language.EN);

        feedbacks().followRename(READER, "ada-lovelace");

        assertThat(rows).extracting(Feedback::authorLogin).containsExactly("ada-lovelace");
    }

    private static List<String> endings(FeedbackPage page) {
        return page.items().stream().map(item -> item.body().value().substring(item.body().value().lastIndexOf(' ') + 1)).toList();
    }

    private static void assertCode(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).code()).isEqualTo(code);
    }
}
