package com.brandonkamga.sourcefolio.shared.web;

import com.brandonkamga.sourcefolio.shared.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** The body of every error response: a stable code to branch on, a message to show. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(ErrorCode code, String message, String path, Map<String, String> fields, String reference) {

    public static ApiError of(ErrorCode code, String message, String path) {
        return new ApiError(code, message, path, Map.of(), null);
    }
}
