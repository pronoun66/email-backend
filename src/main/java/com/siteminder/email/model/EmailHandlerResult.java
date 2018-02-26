package com.siteminder.email.model;

import org.springframework.http.HttpStatus;

public class EmailHandlerResult {

    public enum Type {
        SUCCESS(HttpStatus.OK),
        FAIL(HttpStatus.INTERNAL_SERVER_ERROR),
        ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST);

        private HttpStatus httpStatus;

        Type(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
        }

        public HttpStatus getHttpStatus() {
            return httpStatus;
        }
    }

    private Type type;
    private String message;

    public EmailHandlerResult(Type type) {
        this.type = type;
    }

    public EmailHandlerResult(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
