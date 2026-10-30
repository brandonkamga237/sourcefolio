package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.util.Objects;

/** A preview stored in the bucket: its key, to replace or delete it, and where browsers read it. */
public record PreviewMedia(String key, String url) {

    public PreviewMedia {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(url, "url");
    }
}
