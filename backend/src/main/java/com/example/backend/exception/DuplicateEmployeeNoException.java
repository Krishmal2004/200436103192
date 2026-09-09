package com.example.backend.exception;

public class DuplicateEmployeeNoException extends RuntimeException {

    public DuplicateEmployeeNoException(String employeeNo) {
        super("An officer with employee number '" + employeeNo + "' already exists.");
    }
}
