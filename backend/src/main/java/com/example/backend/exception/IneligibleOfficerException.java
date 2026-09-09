package com.example.backend.exception;

// Thrown when an officer fails one of a programme's eligibility rules.
// See docs/task03_workflow.md, section 4.
public class IneligibleOfficerException extends RuntimeException {

    public IneligibleOfficerException(String message) {
        super(message);
    }
}
