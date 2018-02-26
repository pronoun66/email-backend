package com.siteminder.email.type;

public enum ErrorType {
    FROM_EMPTY_OR_INVALID("From is empty or invalid"),
    TO_EMPTY_OR_INVALID("To is empty or invalid"),
    CC_INVALID("Cc is empty or invalid"),
    BCC_INVALID("Bcc is empty or invalid"),
    SUBJECT_EMPTY("Subject is empty or invalid"),
    CONTENT_EMPTY("Content is empty or invalid"),
    REQUEST_FAILED("Request failed. Please try again"),
    ;

    ErrorType(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }
}
