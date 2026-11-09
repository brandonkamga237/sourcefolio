package com.brandonkamga.sourcefolio.discovery.domain.port;

import com.brandonkamga.sourcefolio.discovery.domain.model.CityCount;
import com.brandonkamga.sourcefolio.discovery.domain.model.ProjectCount;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingSort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RankingStore {

    void saveIndicators(UUID memberId, String login, String name, Instant analyzedAt, Instant accountCreatedAt,
                        int publicContributions, int activeMonths, List<Integer> months, int stars, int followers,
                        int mergedPullRequestsElsewhere, List<String> languages, List<String> externalRepositories);

    void saveProfile(UUID memberId, String countryCode, String city, boolean published);

    /** Listed rows only: published and analysed. cursor is null for the first page. */
    RankingPage page(RankingFilter filter, RankingSort sort, String cursor, int size);

    RankingFacets facets(RankingFilter filter);

    /** Listed profiles per country and city, most first; bounded, so free text cannot flood the map. */
    List<CityCount> cityCounts();

    /** Repositories where listed profiles have merged work, the most shared first; bounded. */
    List<ProjectCount> acceptedProjects();
}
