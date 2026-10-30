package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseStatus;
import com.brandonkamga.sourcefolio.showcase.domain.port.MemberDirectory;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import java.util.Optional;

public class PublicShowcasesImpl implements PublicShowcases {

    private final ShowcaseStore showcases;
    private final MemberDirectory members;

    public PublicShowcasesImpl(ShowcaseStore showcases, MemberDirectory members) {
        this.showcases = showcases;
        this.members = members;
    }

    @Override
    public Optional<PublicShowcase> findBySlug(String slug) {
        return ShowcaseSlug.parse(slug)
                .flatMap(showcases::findBySlug)
                .filter(showcase -> showcase.status() != ShowcaseStatus.REMOVED)
                .map(showcase -> new PublicShowcase(showcase, members.loginOf(showcase.ownerId())));
    }
}
