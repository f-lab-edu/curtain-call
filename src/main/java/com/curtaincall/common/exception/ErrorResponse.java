package com.curtaincall.common.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String message;
    private final int status;

    private ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status);
    }
}

