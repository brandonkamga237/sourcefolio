package com.brandonkamga.sourcefolio.shared.exception;

import java.util.Arrays;

/**
 * Everything the application refuses on purpose. Thrown from plain Java (domain and use cases),
 * so it carries a translation key and its arguments, never an HTTP status or a sentence: the
 * web layer picks the status from the code and the words from the request's language.
 */
public abstract class AppException extends RuntimeException {

    private final ErrorCode code;
    private final String messageKey;
    private final Object[] arguments;

    protected AppException(ErrorCode code, String messageKey, Object... arguments) {
        super(messageKey + (arguments.length == 0 ? "" : " " + Arrays.toString(arguments)));
        this.code = code;
        this.messageKey = messageKey;
        this.arguments = arguments.clone();
    }

    public ErrorCode code() {
        return code;
    }

    public String messageKey() {
        return messageKey;
    }

    public Object[] arguments() {
        return arguments.clone();
    }
}
