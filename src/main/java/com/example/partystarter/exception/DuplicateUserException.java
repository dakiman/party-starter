package com.example.partystarter.exception;

import org.springframework.http.HttpStatus;

public class DuplicateUserException extends ResourceException {
    public DuplicateUserException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
} 