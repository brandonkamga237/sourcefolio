package com.brandonkamga.sourcefolio.engagement.api.dto;

import com.brandonkamga.sourcefolio.engagement.application.LikeState;

public record LikeResponse(int count, boolean liked) {

    public static LikeResponse of(LikeState state) {
        return new LikeResponse(state.count(), state.liked());
    }
}
