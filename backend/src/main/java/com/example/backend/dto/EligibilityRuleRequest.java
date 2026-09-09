package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record EligibilityRuleRequest(
        // One of: DEPARTMENT, GRADE, MIN_YEARS_OF_SERVICE, COOLDOWN_MONTHS
        @NotBlank String ruleType,
        // A department_id (for DEPARTMENT), a grade name (for GRADE), or a
        // non-negative integer as text (for the other two types).
        @NotBlank String ruleValue
) {
}
