package com.example.demo.exception;

public class MissingDescriptionException extends RuntimeException {
    public MissingDescriptionException(String message) {
        super(message);
    }
}
