package com.brandonkamga.sourcefolio.shared.exception;

public class ConflictException extends AppException {

    public ConflictException() {
        super(ErrorCode.CONFLICT, "error.conflict");
    }

    public ConflictException(String messageKey, Object... arguments) {
        super(ErrorCode.CONFLICT, messageKey, arguments);
    }
}
