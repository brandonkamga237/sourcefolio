package com.brandonkamga.sourcefolio.discovery.application;

import com.brandonkamga.sourcefolio.discovery.domain.model.CatalogEntry;
import com.brandonkamga.sourcefolio.discovery.domain.model.MemberCard;
import com.brandonkamga.sourcefolio.discovery.domain.port.CatalogStore;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MembersImpl implements Members {

    private final CatalogStore store;

    public MembersImpl(CatalogStore store) {
        this.store = store;
    }

    // One query: the member's cards. The newest one is shown; the oldest says since when.
    @Override
    public Optional<MemberCard> findByLogin(String login) {
        List<CatalogEntry> cards = store.byOwnerLogin(login);
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        CatalogEntry newest = cards.getFirst();
        return Optional.of(new MemberCard(newest.ownerLogin(), newest.countryCode(), newest.specialties(), newest.headline(), newest.authorRole(),
                cards.stream().map(CatalogEntry::publishedAt).min(Comparator.naturalOrder()).orElseThrow(),
                cards.stream().mapToInt(CatalogEntry::likes).sum(), newest));
    }
}
