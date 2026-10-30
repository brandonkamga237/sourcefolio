package com.brandonkamga.sourcefolio.shared.exception;

public class UnauthenticatedException extends AppException {

    public UnauthenticatedException() {
        super(ErrorCode.UNAUTHENTICATED, "error.unauthenticated");
    }

    public UnauthenticatedException(String messageKey, Object... arguments) {
        super(ErrorCode.UNAUTHENTICATED, messageKey, arguments);
    }
}
