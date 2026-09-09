package com.example.backend.exception;

// Thrown when an eligibility rule being created/updated has a malformed
// ruleType or ruleValue (e.g. an unknown rule type, or a non-numeric
// MIN_YEARS_OF_SERVICE value).
public class InvalidEligibilityRuleException extends RuntimeException {

    public InvalidEligibilityRuleException(String message) {
        super(message);
    }
}
