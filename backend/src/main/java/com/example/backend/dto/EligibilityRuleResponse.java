package com.example.backend.dto;

public record EligibilityRuleResponse(
        Long id,
        Long programmeId,
        String ruleType,
        String ruleValue,
        // Human-readable form of ruleValue — the department's name when
        // ruleType is DEPARTMENT, otherwise the same as ruleValue.
        String ruleValueLabel
) {
}
