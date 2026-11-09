package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CityCount;
import com.brandonkamga.sourcefolio.discovery.domain.model.ProjectCount;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import java.util.List;

/** Published profiles sorted by one indicator, filtered by country and language. */
public interface Rankings {

    /** sort is a RankingSort name (regularity by default); country an ISO code; size 1 to 100. */
    RankingPage listRanking(String sort, String country, String language, String cursor, int size);

    RankingFacets listFacets(String country, String language);

    List<CityCount> listCityCounts();

    List<ProjectCount> listAcceptedProjects();
}
