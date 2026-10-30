package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogPage;

/** The public catalogue. The spec allows two orders only: Discover and Recent, never popularity. */
public interface Catalog {

    int MAX_PAGE_SIZE = 50;

    /** The feed shuffled once a day, one card in five younger than 14 days. */
    CatalogPage listDiscover(String cursor, int size);

    /** Newest publications first. */
    CatalogPage listRecent(String cursor, int size);
}
