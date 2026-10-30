package com.brandonkamga.sourcefolio.storage.domain.port;

/** The bucket. Network calls: never inside a database transaction. */
public interface ObjectStorage {

    void put(String key, byte[] content, String contentType);

    /** Removing a missing object is not an error. */
    void delete(String key);
}
