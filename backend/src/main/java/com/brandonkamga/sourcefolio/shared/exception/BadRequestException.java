package com.brandonkamga.sourcefolio.shared.exception;

public class BadRequestException extends AppException {

    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST, "error.bad-request");
    }

    public BadRequestException(String messageKey, Object... arguments) {
        super(ErrorCode.BAD_REQUEST, messageKey, arguments);
    }
}
