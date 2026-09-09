package com.example.backend.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.EligibilityRuleRequest;
import com.example.backend.dto.EligibilityRuleResponse;
import com.example.backend.entity.Department;
import com.example.backend.entity.EligibilityRuleType;
import com.example.backend.entity.NominationStatus;
import com.example.backend.entity.Officer;
import com.example.backend.entity.ProgrammeEligibilityRule;
import com.example.backend.entity.TrainingProgramme;
import com.example.backend.exception.InvalidEligibilityRuleException;
import com.example.backend.exception.IneligibleOfficerException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.DepartmentRepository;
import com.example.backend.repository.NominationRepository;
import com.example.backend.repository.ProgrammeEligibilityRuleRepository;
import com.example.backend.repository.TrainingProgrammeRepository;

// The generic rule engine described in docs/task03_workflow.md: reads
// whichever ProgrammeEligibilityRule rows exist for a programme (data) and
// evaluates an officer against them, rather than hardcoding per-programme
// checks in Java. Adding a new eligibility requirement for a programme is
// an insert into programme_eligibility_rules, not a code change.
@Service
public class EligibilityService {

    // Applied when a programme has no explicit COOLDOWN_MONTHS row — the
    // "12 months" rule is a blanket default, not a per-programme example
    // like the other three rule types. See docs/task03_workflow.md, section 3.
    private static final int DEFAULT_COOLDOWN_MONTHS = 12;

    private final ProgrammeEligibilityRuleRepository ruleRepository;
    private final NominationRepository nominationRepository;
    private final DepartmentRepository departmentRepository;
    private final TrainingProgrammeRepository programmeRepository;

    public EligibilityService(
            ProgrammeEligibilityRuleRepository ruleRepository,
            NominationRepository nominationRepository,
            DepartmentRepository departmentRepository,
            TrainingProgrammeRepository programmeRepository
    ) {
        this.ruleRepository = ruleRepository;
        this.nominationRepository = nominationRepository;
        this.departmentRepository = departmentRepository;
        this.programmeRepository = programmeRepository;
    }

    // Throws IneligibleOfficerException on the first rule the officer fails.
    // Called from NominationService.createNomination before the Task 1
    // duplicate check and Task 2 capacity check. See section 4 of the doc.
    @Transactional(readOnly = true)
    public void checkEligible(Officer officer, TrainingProgramme programme) {
        List<ProgrammeEligibilityRule> rules = ruleRepository.findByProgrammeId(programme.getId());

        checkDepartment(officer, rules);
        checkGrade(officer, rules);
        checkMinYearsOfService(officer, rules);
        checkCooldown(officer, programme, rules);
    }

    private void checkDepartment(Officer officer, List<ProgrammeEligibilityRule> rules) {
        List<ProgrammeEligibilityRule> departmentRules = rulesOfType(rules, EligibilityRuleType.DEPARTMENT);
        if (departmentRules.isEmpty()) {
            return;
        }
        String officerDepartmentId = String.valueOf(officer.getDepartment().getId());
        boolean matches = departmentRules.stream().anyMatch(r -> r.getRuleValue().equals(officerDepartmentId));
        if (!matches) {
            String allowed = departmentRules.stream()
                    .map(r -> departmentName(r.getRuleValue()))
                    .collect(Collectors.joining(", "));
            throw new IneligibleOfficerException(
                    "Officer's department is not eligible for this programme (requires: " + allowed + ").");
        }
    }

    private void checkGrade(Officer officer, List<ProgrammeEligibilityRule> rules) {
        List<ProgrammeEligibilityRule> gradeRules = rulesOfType(rules, EligibilityRuleType.GRADE);
        if (gradeRules.isEmpty()) {
            return;
        }
        boolean matches = officer.getGrade() != null
                && gradeRules.stream().anyMatch(r -> r.getRuleValue().equalsIgnoreCase(officer.getGrade()));
        if (!matches) {
            String allowed = gradeRules.stream().map(ProgrammeEligibilityRule::getRuleValue)
                    .collect(Collectors.joining(", "));
            throw new IneligibleOfficerException(
                    "Officer's grade is not eligible for this programme (requires: " + allowed + ").");
        }
    }

    private void checkMinYearsOfService(Officer officer, List<ProgrammeEligibilityRule> rules) {
        rulesOfType(rules, EligibilityRuleType.MIN_YEARS_OF_SERVICE).stream().findFirst().ifPresent(rule -> {
            int required = Integer.parseInt(rule.getRuleValue());
            if (officer.getJoinedDate() == null) {
                throw new IneligibleOfficerException(
                        "Officer's years of service is not on record; this programme requires at least "
                                + required + " years.");
            }
            long actualYears = ChronoUnit.YEARS.between(officer.getJoinedDate(), LocalDate.now());
            if (actualYears < required) {
                throw new IneligibleOfficerException(
                        "Requires at least " + required + " years of service; officer has " + actualYears + ".");
            }
        });
    }

    private void checkCooldown(Officer officer, TrainingProgramme programme, List<ProgrammeEligibilityRule> rules) {
        int cooldownMonths = rulesOfType(rules, EligibilityRuleType.COOLDOWN_MONTHS).stream()
                .findFirst()
                .map(r -> Integer.parseInt(r.getRuleValue()))
                .orElse(DEFAULT_COOLDOWN_MONTHS);

        if (cooldownMonths <= 0 || programme.getProgrammeCode() == null) {
            return;
        }

        nominationRepository
                .findFirstByOfficer_IdAndProgramme_ProgrammeCodeAndStatusOrderByProgramme_TrainingDateDesc(
                        officer.getId(), programme.getProgrammeCode(), NominationStatus.CONFIRMED)
                .ifPresent(previous -> {
                    LocalDate previousDate = previous.getProgramme().getTrainingDate();
                    LocalDate cutoff = LocalDate.now().minusMonths(cooldownMonths);
                    if (!previousDate.isBefore(cutoff)) {
                        throw new IneligibleOfficerException(
                                "Officer already participated in this programme on " + previousDate
                                        + "; not eligible again until " + previousDate.plusMonths(cooldownMonths) + ".");
                    }
                });
    }

    private List<ProgrammeEligibilityRule> rulesOfType(List<ProgrammeEligibilityRule> rules, EligibilityRuleType type) {
        return rules.stream().filter(r -> r.getRuleType() == type).toList();
    }

    private String departmentName(String departmentIdAsString) {
        try {
            return departmentRepository.findById(Long.parseLong(departmentIdAsString))
                    .map(Department::getName)
                    .orElse(departmentIdAsString);
        } catch (NumberFormatException e) {
            return departmentIdAsString;
        }
    }

    // --- Rule management (see docs/task03_workflow.md, section 5) ---

    @Transactional(readOnly = true)
    public List<EligibilityRuleResponse> listRules(Long programmeId) {
        requireProgramme(programmeId);
        return ruleRepository.findByProgrammeId(programmeId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public EligibilityRuleResponse addRule(Long programmeId, EligibilityRuleRequest request) {
        TrainingProgramme programme = requireProgramme(programmeId);
        EligibilityRuleType ruleType = parseRuleType(request.ruleType());
        validateRuleValue(ruleType, request.ruleValue());

        ProgrammeEligibilityRule rule = new ProgrammeEligibilityRule(programme, ruleType, request.ruleValue());
        return toResponse(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long programmeId, Long ruleId) {
        requireProgramme(programmeId);
        ProgrammeEligibilityRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Eligibility rule not found: " + ruleId));
        if (!rule.getProgramme().getId().equals(programmeId)) {
            throw new ResourceNotFoundException("Eligibility rule not found: " + ruleId);
        }
        ruleRepository.delete(rule);
    }

    private TrainingProgramme requireProgramme(Long programmeId) {
        return programmeRepository.findById(programmeId)
                .orElseThrow(() -> new ResourceNotFoundException("Training programme not found: " + programmeId));
    }

    private EligibilityRuleType parseRuleType(String raw) {
        try {
            return EligibilityRuleType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidEligibilityRuleException(
                    "Unknown rule type '" + raw + "'. Must be one of: DEPARTMENT, GRADE, MIN_YEARS_OF_SERVICE, COOLDOWN_MONTHS.");
        }
    }

    private void validateRuleValue(EligibilityRuleType ruleType, String ruleValue) {
        switch (ruleType) {
            case DEPARTMENT -> {
                long departmentId = parseLongOrThrow(ruleValue, "DEPARTMENT rule value must be a department id.");
                if (departmentRepository.findById(departmentId).isEmpty()) {
                    throw new ResourceNotFoundException("Department not found: " + departmentId);
                }
            }
            case MIN_YEARS_OF_SERVICE, COOLDOWN_MONTHS -> {
                int value = (int) parseLongOrThrow(ruleValue, ruleType + " rule value must be a whole number.");
                if (value < 0) {
                    throw new InvalidEligibilityRuleException(ruleType + " rule value must not be negative.");
                }
            }
            case GRADE -> {
                // Any non-blank string is accepted — already enforced by @NotBlank on the request.
            }
        }
    }

    private long parseLongOrThrow(String value, String message) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidEligibilityRuleException(message);
        }
    }

    private EligibilityRuleResponse toResponse(ProgrammeEligibilityRule rule) {
        String label = rule.getRuleType() == EligibilityRuleType.DEPARTMENT
                ? departmentName(rule.getRuleValue())
                : rule.getRuleValue();
        return new EligibilityRuleResponse(
                rule.getId(),
                rule.getProgramme().getId(),
                rule.getRuleType().name(),
                rule.getRuleValue(),
                label
        );
    }
}
