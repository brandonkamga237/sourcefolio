package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankedEntry;
import java.util.List;

/** The public catalogue. The spec allows two orders only: Discover and Recent, never popularity. */
public interface Catalog {

    int MAX_PAGE_SIZE = 50;

    /** The feed shuffled once a day, one card in five younger than 14 days. */
    CatalogPage listDiscover(String cursor, int size);

    /** Newest publications first. */
    CatalogPage listRecent(String cursor, int size);

    int MAX_RANKING_SIZE = 100;

    /** The Ranking tab: the most liked showcases, at most 100. */
    List<RankedEntry> listRanking(int size);
}
