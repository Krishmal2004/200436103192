package com.example.backend.entity;

// See docs/task03_workflow.md, section 3 — a small fixed set of rule
// shapes, composed generically, rather than one hardcoded check per
// named programme.
public enum EligibilityRuleType {
    DEPARTMENT,
    GRADE,
    MIN_YEARS_OF_SERVICE,
    COOLDOWN_MONTHS
}
