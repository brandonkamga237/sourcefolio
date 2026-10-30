package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.DiscoverCursor;
import com.brandonkamga.sourcefolio.discovery.domain.model.DiscoverFeed;
import com.brandonkamga.sourcefolio.discovery.domain.model.RecentCursor;
import com.brandonkamga.sourcefolio.discovery.domain.model.Shuffled;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public class CatalogImpl implements Catalog {

    private final CatalogStore store;
    private final Clock clock;

    public CatalogImpl(CatalogStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    @Override
    public CatalogPage listDiscover(String rawCursor, int size) {
        int limit = clamp(size);
        DiscoverCursor cursor = DiscoverCursor.decode(rawCursor)
                .orElseGet(() -> DiscoverCursor.start(LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC)));
        // From the cursor's day, not from now: a showcase must not cross from one list to the
        // other while someone scrolls, or it would be shown twice or skipped.
        Instant youngSince = cursor.day().atStartOfDay(ZoneOffset.UTC).toInstant().minus(DiscoverFeed.YOUNG);
        // One more than needed from each list, to know whether a next page exists.
        List<Shuffled> young = store.shuffled(cursor.day(), true, youngSince, cursor.youngAfter(), limit + 1);
        List<Shuffled> old = store.shuffled(cursor.day(), false, youngSince, cursor.oldAfter(), limit + 1);
        DiscoverFeed.Page page = DiscoverFeed.interleave(young, old, cursor.index(), limit);

        boolean more = page.youngUsed() < young.size() || page.oldUsed() < old.size();
        String next = more ? new DiscoverCursor(cursor.day(), cursor.index() + page.items().size(),
                lastKey(young, page.youngUsed(), cursor.youngAfter()),
                lastKey(old, page.oldUsed(), cursor.oldAfter())).encode() : null;
        return new CatalogPage(page.items().stream().map(Shuffled::entry).toList(), next);
    }

    @Override
    public CatalogPage listRecent(String rawCursor, int size) {
        int limit = clamp(size);
        List<CatalogEntry> rows = store.recent(RecentCursor.decode(rawCursor).orElse(null), limit + 1);
        if (rows.size() <= limit) {
            return new CatalogPage(rows, null);
        }
        List<CatalogEntry> page = rows.subList(0, limit);
        CatalogEntry last = page.getLast();
        return new CatalogPage(page, new RecentCursor(last.publishedAt(), last.showcaseId()).encode());
    }

    private static String lastKey(List<Shuffled> list, int used, String previous) {
        return used == 0 ? previous : list.get(used - 1).key();
    }

    private static int clamp(int size) {
        return Math.clamp(size, 1, MAX_PAGE_SIZE);
    }
}
