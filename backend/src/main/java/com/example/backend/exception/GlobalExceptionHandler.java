package com.example.backend.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateNominationException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateNominationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "DUPLICATE_NOMINATION");
        body.put("message", ex.getMessage());
        body.put("existingDepartment", ex.getExistingDepartmentName());
        body.put("existingNominatedAt", ex.getExistingNominatedAt());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "NOT_FOUND");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DuplicateEmployeeNoException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmployeeNo(DuplicateEmployeeNoException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "DUPLICATE_EMPLOYEE_NO");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidNominationStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidNominationStateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "INVALID_STATE");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IneligibleOfficerException.class)
    public ResponseEntity<Map<String, Object>> handleIneligible(IneligibleOfficerException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "INELIGIBLE_OFFICER");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidEligibilityRuleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRule(InvalidEligibilityRuleException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "INVALID_ELIGIBILITY_RULE");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "VALIDATION_ERROR");
        body.put("message", ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", ")));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
