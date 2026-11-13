package com.example.api.exception;

import org.springframework.http.HttpStatus;

public class CotizacionException extends RuntimeException {

    private final HttpStatus status;

    public CotizacionException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
