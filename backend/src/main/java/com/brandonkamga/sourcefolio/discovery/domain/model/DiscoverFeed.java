package com.brandonkamga.sourcefolio.discovery.domain.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * The Discover rule of the spec: one card in five is reserved for showcases younger than 14
 * days, so a newcomer is seen even among many older portfolios. Each list arrives already in the
 * day's shuffled order; this decides which list fills each position of the feed. When one list
 * runs out, the other fills its positions, so no position stays empty.
 */
public final class DiscoverFeed {

    public static final Duration YOUNG = Duration.ofDays(14);
    private static final int RESERVED_EVERY = 5;

    private DiscoverFeed() {
    }

    /** What filled a page, and how far each list was read. */
    public record Page(List<Shuffled> items, int youngUsed, int oldUsed) {
    }

    /**
     * Fills {@code size} positions starting at feed position {@code startIndex} (0-based).
     * Positions 4, 9, 14… take a young showcase first; the others take an older one first.
     */
    public static Page interleave(List<Shuffled> young, List<Shuffled> old, long startIndex, int size) {
        List<Shuffled> items = new ArrayList<>(size);
        int youngUsed = 0;
        int oldUsed = 0;
        for (long position = startIndex; items.size() < size; position++) {
            boolean reserved = position % RESERVED_EVERY == RESERVED_EVERY - 1;
            boolean youngLeft = youngUsed < young.size();
            boolean oldLeft = oldUsed < old.size();
            if (!youngLeft && !oldLeft) {
                break;
            }
            if (youngLeft && (reserved || !oldLeft)) {
                items.add(young.get(youngUsed++));
            } else {
                items.add(old.get(oldUsed++));
            }
        }
        return new Page(List.copyOf(items), youngUsed, oldUsed);
    }
}
