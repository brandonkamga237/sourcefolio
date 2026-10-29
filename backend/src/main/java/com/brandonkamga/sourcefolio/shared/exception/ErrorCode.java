package com.brandonkamga.sourcefolio.shared.exception;

/**
 * A stable name for what went wrong. The message is prose in the reader's language and will be
 * reworded; this will not. A client that must behave differently — send someone to sign in,
 * highlight a field — branches on this, never on the sentence.
 */
public enum ErrorCode {
    NOT_FOUND,
    BAD_REQUEST,
    /** Field validation failed; the response carries the field-to-message map. */
    VALIDATION_FAILED,
    /** The state on the server contradicts what the request assumed. */
    CONFLICT,
    FORBIDDEN,
    UNAUTHENTICATED,
    MALFORMED_REQUEST,
    METHOD_NOT_ALLOWED,
    /** Something broke on our side. The response carries a reference to find the log line. */
    INTERNAL_ERROR,
}
