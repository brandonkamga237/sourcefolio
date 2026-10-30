package com.brandonkamga.sourcefolio.shared.exception;

public class NotFoundException extends AppException {

    public NotFoundException() {
        super(ErrorCode.NOT_FOUND, "error.not-found");
    }

    public NotFoundException(String messageKey, Object... arguments) {
        super(ErrorCode.NOT_FOUND, messageKey, arguments);
    }
}
