package com.brandonkamga.sourcefolio.engagement.api.dto;

import com.brandonkamga.sourcefolio.engagement.application.FeedbackPage;
import java.util.List;
import java.util.UUID;

public record FeedbackPageResponse(List<FeedbackResponse> items, String nextCursor, int total) {

    public static FeedbackPageResponse of(FeedbackPage page, UUID readerId) {
        return new FeedbackPageResponse(page.items().stream().map(item -> FeedbackResponse.of(item, readerId)).toList(),
                page.nextCursor(), page.total());
    }
}
