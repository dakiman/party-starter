package com.example.partystarter.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ResourceException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}