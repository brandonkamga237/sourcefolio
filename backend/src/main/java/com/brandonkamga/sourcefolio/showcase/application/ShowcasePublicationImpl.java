package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.checkup.application.SiteReachability;
import com.brandonkamga.sourcefolio.shared.exception.ConflictException;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryProvider;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import com.brandonkamga.sourcefolio.showcase.domain.port.MemberDirectory;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShowcasePublicationImpl implements ShowcasePublication {

    private final ShowcaseStore showcases;
    private final CandidateStore candidates;
    private final SiteReachability sites;
    private final MemberDirectory members;
    private final ShowcaseEvents events;
    private final Clock clock;

    public ShowcasePublicationImpl(ShowcaseStore showcases, CandidateStore candidates, SiteReachability sites,
                                   MemberDirectory members, ShowcaseEvents events, Clock clock) {
        this.showcases = showcases;
        this.candidates = candidates;
        this.sites = sites;
        this.members = members;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public Showcase publish(PublishShowcase command) {
        if (showcases.findByOwner(command.memberId()).isPresent()) {
            throw new ConflictException("showcase.already-published");
        }
        // Detection only lists repositories the member owns, and only sites taken from their
        // homepage field or their GitHub Pages: a candidate meets the ownership rules of
        // automatic publication by construction. Anything else is not publishable from here.
        ShowcaseCandidate candidate = candidates.find(command.memberId()).stream()
                .map(CandidateScan::candidates)
                .flatMap(List::stream)
                .filter(found -> found.repository().sameAs(command.repository()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("showcase.candidate-not-found"));
        if (showcases.existsByRepository(RepositoryProvider.GITHUB, candidate.repository())) {
            throw new ConflictException("showcase.repository-taken");
        }
        if (!sites.isReachable(candidate.siteUrl().value())) {
            throw new ConflictException("showcase.site-unreachable", candidate.siteUrl().value());
        }
        String login = members.loginOf(command.memberId());
        Showcase published = showcases.save(Showcase.publishVerified(command.memberId(), freeSlug(login), candidate, clock.instant()));
        events.showcasePublished(new ShowcasePublished(published.id().value(), published.slug().value(),
                published.siteUrl().value(), published.sourceRepo().coordinates().toString(), published.sourceRepo().url(),
                published.ownerId(), login, published.publishedAt()));
        return published;
    }

    // One query in nearly every case: the login is free unless an earlier member held it, which
    // takes a GitHub rename, so the loop stays bounded by the rare collisions.
    private ShowcaseSlug freeSlug(String login) {
        ShowcaseSlug base = ShowcaseSlug.fromLogin(login);
        ShowcaseSlug slug = base;
        for (int number = 2; showcases.existsBySlug(slug); number++) {
            slug = base.withSuffix(number);
        }
        return slug;
    }

    @Override
    public Optional<Showcase> findMyShowcase(UUID memberId) {
        return showcases.findByOwner(memberId);
    }
}
