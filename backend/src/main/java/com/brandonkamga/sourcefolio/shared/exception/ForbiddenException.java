package com.brandonkamga.sourcefolio.shared.exception;

public class ForbiddenException extends AppException {

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN, "error.forbidden");
    }

    public ForbiddenException(String messageKey, Object... arguments) {
        super(ErrorCode.FORBIDDEN, messageKey, arguments);
    }
}
