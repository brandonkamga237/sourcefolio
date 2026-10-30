package com.brandonkamga.sourcefolio.storage.application;

/** What other modules may do with the bucket. Makes network calls: never inside a transaction. */
public interface MediaStore {

    /** Stores the content under a new key in {@code folder}, so a cached copy is never stale. */
    StoredMedia store(String folder, byte[] content, String contentType, String extension);

    void delete(String key);
}
