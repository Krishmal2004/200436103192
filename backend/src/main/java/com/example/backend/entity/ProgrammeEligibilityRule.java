package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// One eligibility condition attached to a programme. A programme with no
// rows of a given rule_type is unrestricted on that axis; multiple rows of
// the same type are OR'd together, different types are AND'd together.
// See docs/task03_workflow.md, section 3.
@Entity
@Table(name = "programme_eligibility_rules")
public class ProgrammeEligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private TrainingProgramme programme;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private EligibilityRuleType ruleType;

    // Holds a department_id (for DEPARTMENT), a grade name (for GRADE), or
    // an integer as text (for MIN_YEARS_OF_SERVICE / COOLDOWN_MONTHS).
    @Column(name = "rule_value", nullable = false)
    private String ruleValue;

    public ProgrammeEligibilityRule() {
    }

    public ProgrammeEligibilityRule(TrainingProgramme programme, EligibilityRuleType ruleType, String ruleValue) {
        this.programme = programme;
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingProgramme getProgramme() {
        return programme;
    }

    public void setProgramme(TrainingProgramme programme) {
        this.programme = programme;
    }

    public EligibilityRuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(EligibilityRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }
}
