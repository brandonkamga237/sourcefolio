package com.brandonkamga.sourcefolio.storage.application;

/** An object in the bucket: its key, to delete it later, and where browsers read it. */
public record StoredMedia(String key, String url) {
}
