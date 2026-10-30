package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The detection rules of the spec ("Connexion et publication en 1 clic"): the member's
 * {login}.github.io repository, repositories whose homepage field points to a site, and
 * repositories named portfolio, site or me. Each site appears once, under its strongest reason;
 * within a reason, the most recently pushed repository comes first.
 */
public final class CandidateDetection {

    private static final Set<String> PORTFOLIO_NAMES = Set.of("portfolio", "site", "me");

    private CandidateDetection() {
    }

    public static List<ShowcaseCandidate> detect(String login, List<PublicRepository> repositories) {
        String pagesHost = login.toLowerCase(Locale.ROOT) + ".github.io";
        List<PublicRepository> recentFirst = new ArrayList<>(repositories);
        recentFirst.sort(Comparator.comparing(PublicRepository::pushedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        Map<SiteUrl, ShowcaseCandidate> bySite = new LinkedHashMap<>();
        for (PublicRepository repository : recentFirst) {
            candidate(repository, pagesHost).ifPresent(found -> bySite.merge(found.siteUrl(), found,
                    (kept, other) -> other.reason().ordinal() < kept.reason().ordinal() ? other : kept));
        }
        return bySite.values().stream()
                .sorted(Comparator.comparing((ShowcaseCandidate found) -> found.reason().ordinal()))
                .toList();
    }

    private static Optional<ShowcaseCandidate> candidate(PublicRepository repository, String pagesHost) {
        String name = repository.coordinates().name().toLowerCase(Locale.ROOT);
        Optional<SiteUrl> homepage = SiteUrl.parse(repository.homepage());

        if (name.equals(pagesHost)) {
            return Optional.of(new ShowcaseCandidate(repository.coordinates(),
                    homepage.orElseGet(() -> SiteUrl.parse("https://" + pagesHost).orElseThrow()), CandidateReason.USER_PAGES));
        }
        Optional<SiteUrl> projectPages = repository.hasPages()
                ? SiteUrl.parse("https://" + pagesHost + "/" + repository.coordinates().name() + "/")
                : Optional.empty();
        if (PORTFOLIO_NAMES.contains(name)) {
            return homepage.or(() -> projectPages)
                    .map(site -> new ShowcaseCandidate(repository.coordinates(), site, CandidateReason.NAMED_REPOSITORY));
        }
        return homepage.map(site -> new ShowcaseCandidate(repository.coordinates(), site, CandidateReason.HOMEPAGE))
                .or(() -> projectPages.map(site -> new ShowcaseCandidate(repository.coordinates(), site, CandidateReason.PROJECT_PAGES)));
    }
}
