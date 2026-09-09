package com.example.backend.exception;

public class InvalidNominationStateException extends RuntimeException {

    public InvalidNominationStateException(String message) {
        super(message);
    }
}
