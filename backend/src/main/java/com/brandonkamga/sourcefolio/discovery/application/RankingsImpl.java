package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingSort;
import com.brandonkamga.sourcefolio.discovery.domain.port.RankingStore;
import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import java.util.Locale;

public class RankingsImpl implements Rankings {

    static final int MAX_SIZE = 100;

    private final RankingStore store;

    public RankingsImpl(RankingStore store) {
        this.store = store;
    }

    @Override
    public RankingPage listRanking(String sort, String country, String language, String cursor, int size) {
        RankingSort parsed = sort == null || sort.isBlank()
                ? RankingSort.REGULARITY
                : RankingSort.parse(sort).orElseThrow(() -> new BadRequestException("ranking.sort-invalid"));
        try {
            return store.page(filter(country, language), parsed, cursor == null || cursor.isBlank() ? null : cursor,
                    Math.clamp(size, 1, MAX_SIZE));
        } catch (IllegalArgumentException unreadable) {
            throw new BadRequestException("ranking.cursor-invalid");
        }
    }

    @Override
    public RankingFacets listFacets(String country, String language) {
        return store.facets(filter(country, language));
    }

    private static RankingFilter filter(String country, String language) {
        String code = country == null || country.isBlank() ? null : country.strip().toUpperCase(Locale.ROOT);
        if (code != null && !code.matches("[A-Z]{2}")) {
            throw new BadRequestException("ranking.country-invalid");
        }
        return new RankingFilter(code, language == null || language.isBlank() ? null : language.strip());
    }
}
