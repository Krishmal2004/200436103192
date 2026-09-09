package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.EligibilityRuleRequest;
import com.example.backend.dto.NominationRequest;
import com.example.backend.dto.NominationResponse;
import com.example.backend.entity.Department;
import com.example.backend.entity.Officer;
import com.example.backend.entity.TrainingProgramme;
import com.example.backend.exception.IneligibleOfficerException;
import com.example.backend.repository.DepartmentRepository;
import com.example.backend.repository.OfficerRepository;
import com.example.backend.repository.TrainingProgrammeRepository;

/**
 * Reproduces the Task 3 examples from docs/task03_workflow.md: department-,
 * grade-, and years-of-service-restricted programmes, plus the 12-month
 * cooldown rule — all configured as data (ProgrammeEligibilityRule rows),
 * not code. Rolled back at the end like NominationCapacityScenarioTest.
 */
@SpringBootTest
@Transactional
class EligibilityScenarioTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OfficerRepository officerRepository;

    @Autowired
    private TrainingProgrammeRepository programmeRepository;

    @Autowired
    private NominationService nominationService;

    @Autowired
    private EligibilityService eligibilityService;

    @Test
    void departmentRestrictedProgramme_onlyAllowsListedDepartments() {
        Department finance = departmentRepository.save(new Department("Eligibility Test Finance"));
        Department it = departmentRepository.save(new Department("Eligibility Test IT"));

        Officer financeOfficer = officerRepository.save(new Officer("ELG-001", "Finance Officer", finance));
        Officer itOfficer = officerRepository.save(new Officer("ELG-002", "IT Officer", it));

        TrainingProgramme programme = programme("Financial Management Programme", "FIN-MGMT-TEST", 10, LocalDate.now().plusDays(30));
        eligibilityService.addRule(programme.getId(), new EligibilityRuleRequest("DEPARTMENT", String.valueOf(finance.getId())));

        // Officer from an eligible department: succeeds.
        NominationResponse confirmed = nominationService.createNomination(
                new NominationRequest(programme.getId(), financeOfficer.getId(), finance.getId(), "test"));
        assertThat(confirmed.status()).isEqualTo("CONFIRMED");

        // Officer from an ineligible department: rejected before duplicate/capacity checks even run.
        assertThatThrownBy(() -> nominationService.createNomination(
                new NominationRequest(programme.getId(), itOfficer.getId(), it.getId(), "test")))
                .isInstanceOf(IneligibleOfficerException.class)
                .hasMessageContaining("department is not eligible");
    }

    @Test
    void gradeAndYearsOfServiceProgramme_requiresBoth() {
        Department admin = departmentRepository.save(new Department("Eligibility Test Admin"));

        Officer juniorOfficer = officerRepository.save(withGrade("ELG-010", "Junior", admin, "Officer", LocalDate.now().minusYears(6)));
        Officer tooNewSenior = officerRepository.save(withGrade("ELG-011", "New Senior", admin, "Senior Officer", LocalDate.now().minusYears(2)));
        Officer eligibleSenior = officerRepository.save(withGrade("ELG-012", "Eligible Senior", admin, "Senior Officer", LocalDate.now().minusYears(6)));

        TrainingProgramme programme = programme("Management Development Programme", "MGMT-DEV-TEST", 10, LocalDate.now().plusDays(30));
        eligibilityService.addRule(programme.getId(), new EligibilityRuleRequest("GRADE", "Senior Officer"));
        eligibilityService.addRule(programme.getId(), new EligibilityRuleRequest("MIN_YEARS_OF_SERVICE", "5"));

        // Wrong grade: rejected on the grade check.
        assertThatThrownBy(() -> nominationService.createNomination(
                new NominationRequest(programme.getId(), juniorOfficer.getId(), admin.getId(), "test")))
                .isInstanceOf(IneligibleOfficerException.class)
                .hasMessageContaining("grade is not eligible");

        // Right grade, not enough service: rejected on the years-of-service check.
        assertThatThrownBy(() -> nominationService.createNomination(
                new NominationRequest(programme.getId(), tooNewSenior.getId(), admin.getId(), "test")))
                .isInstanceOf(IneligibleOfficerException.class)
                .hasMessageContaining("years of service");

        // Both satisfied: succeeds.
        NominationResponse confirmed = nominationService.createNomination(
                new NominationRequest(programme.getId(), eligibleSenior.getId(), admin.getId(), "test"));
        assertThat(confirmed.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void cooldown_blocksReRegistrationWithinDefaultWindow_untilOverridden() {
        Department hr = departmentRepository.save(new Department("Eligibility Test HR"));
        Officer officer = officerRepository.save(new Officer("ELG-020", "Repeat Attendee", hr));

        // A past session of the programme, 3 months ago, that this officer attended (CONFIRMED).
        TrainingProgramme pastSession = programme("Cybersecurity Refresher", "CYBER-REFRESH-TEST", 10, LocalDate.now().minusMonths(3));
        NominationResponse pastNomination = nominationService.createNomination(
                new NominationRequest(pastSession.getId(), officer.getId(), hr.getId(), "test"));
        assertThat(pastNomination.status()).isEqualTo("CONFIRMED");

        // A new session of "the same" programme (same programme_code), still within 12 months of the last one.
        TrainingProgramme newSession = programme("Cybersecurity Refresher", "CYBER-REFRESH-TEST", 10, LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> nominationService.createNomination(
                new NominationRequest(newSession.getId(), officer.getId(), hr.getId(), "test")))
                .isInstanceOf(IneligibleOfficerException.class)
                .hasMessageContaining("already participated");

        // Programme opts into a shorter, 1-month cooldown: now eligible again.
        eligibilityService.addRule(newSession.getId(), new EligibilityRuleRequest("COOLDOWN_MONTHS", "1"));
        NominationResponse reNomination = nominationService.createNomination(
                new NominationRequest(newSession.getId(), officer.getId(), hr.getId(), "test"));
        assertThat(reNomination.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void programmeWithNoRules_isOpenToEveryone() {
        Department any = departmentRepository.save(new Department("Eligibility Test Open"));
        Officer officer = officerRepository.save(new Officer("ELG-030", "Anyone", any));

        TrainingProgramme programme = programme("Open Enrollment Workshop", "OPEN-TEST", 10, LocalDate.now().plusDays(30));

        NominationResponse confirmed = nominationService.createNomination(
                new NominationRequest(programme.getId(), officer.getId(), any.getId(), "test"));
        assertThat(confirmed.status()).isEqualTo("CONFIRMED");
    }

    private TrainingProgramme programme(String title, String code, int maxParticipants, LocalDate trainingDate) {
        TrainingProgramme programme = new TrainingProgramme();
        programme.setTitle(title);
        programme.setProgrammeCode(code);
        programme.setTrainingDate(trainingDate);
        programme.setVenue("Test Venue");
        programme.setTrainer("Test Trainer");
        programme.setMaxParticipants(maxParticipants);
        return programmeRepository.save(programme);
    }

    private Officer withGrade(String employeeNo, String name, Department department, String grade, LocalDate joinedDate) {
        Officer officer = new Officer(employeeNo, name, department);
        officer.setGrade(grade);
        officer.setJoinedDate(joinedDate);
        return officer;
    }
}
