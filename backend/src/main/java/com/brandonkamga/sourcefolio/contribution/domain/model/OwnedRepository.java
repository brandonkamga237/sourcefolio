package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.util.List;

/** A public repository the member owns, forks excluded. */
public record OwnedRepository(String name, String description, String url, int stars,
                              String primaryLanguage, List<LanguageBytes> languages) {

    public OwnedRepository {
        languages = List.copyOf(languages);
    }
}
