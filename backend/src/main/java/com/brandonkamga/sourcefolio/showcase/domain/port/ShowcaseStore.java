package com.brandonkamga.sourcefolio.showcase.domain.port;

import java.util.List;
import java.util.UUID;

public interface ShowcaseStore {

    List<String> findMediaKeysByOwner(UUID ownerId);
}
