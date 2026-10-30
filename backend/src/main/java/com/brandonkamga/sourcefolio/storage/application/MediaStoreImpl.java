package com.brandonkamga.sourcefolio.storage.application;

import com.brandonkamga.sourcefolio.storage.domain.port.ObjectStorage;
import java.util.UUID;

public class MediaStoreImpl implements MediaStore {

    private final ObjectStorage storage;
    private final String publicUrl;

    public MediaStoreImpl(ObjectStorage storage, String publicUrl) {
        this.storage = storage;
        this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
    }

    @Override
    public StoredMedia store(String folder, byte[] content, String contentType, String extension) {
        String key = folder + "/" + UUID.randomUUID() + "." + extension;
        storage.put(key, content, contentType);
        return new StoredMedia(key, publicUrl + "/" + key);
    }

    @Override
    public void delete(String key) {
        storage.delete(key);
    }
}
