package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.NominationRequest;
import com.example.backend.dto.NominationResponse;
import com.example.backend.entity.Department;
import com.example.backend.entity.Nomination;
import com.example.backend.entity.NominationStatus;
import com.example.backend.entity.Officer;
import com.example.backend.entity.TrainingProgramme;
import com.example.backend.repository.DepartmentRepository;
import com.example.backend.repository.NominationRepository;
import com.example.backend.repository.OfficerRepository;
import com.example.backend.repository.TrainingProgrammeRepository;

/**
 * Reproduces the exact scenario from docs/task02_workflow.md: a programme
 * with a 40-seat capacity receives 60 valid nominations, and a confirmed
 * cancellation promotes the next person on the waiting list.
 *
 * Runs against the real datasource but stays wrapped in one transaction that
 * is rolled back at the end (see @Transactional below), so it never leaves
 * any of its 60 test officers or nominations behind in the dev database.
 */
@SpringBootTest
@Transactional
class NominationCapacityScenarioTest {

    private static final int MAX_PARTICIPANTS = 40;
    private static final int TOTAL_NOMINATIONS = 60;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OfficerRepository officerRepository;

    @Autowired
    private TrainingProgrammeRepository programmeRepository;

    @Autowired
    private NominationRepository nominationRepository;

    @Autowired
    private NominationService nominationService;

    @Test
    void first40NominationsConfirmedRestWaitlisted_thenCancellationPromotesNextInLine() {
        Department department = departmentRepository.save(new Department("Cybersecurity Scenario Test Dept"));

        TrainingProgramme programme = new TrainingProgramme();
        programme.setTitle("Cybersecurity Awareness Programme");
        programme.setTrainingDate(LocalDate.now().plusDays(30));
        programme.setVenue("Main Auditorium");
        programme.setTrainer("Dr. C. Amarasinghe");
        programme.setMaxParticipants(MAX_PARTICIPANTS);
        programme = programmeRepository.save(programme);

        // Nominate 60 distinct officers, in order, for the one 40-seat programme.
        List<NominationResponse> responses = new ArrayList<>();
        for (int i = 1; i <= TOTAL_NOMINATIONS; i++) {
            Officer officer = officerRepository.save(
                    new Officer(String.format("CYB-%03d", i), "Test Officer " + i, department));

            NominationRequest request = new NominationRequest(
                    programme.getId(), officer.getId(), department.getId(), "Scenario test");
            responses.add(nominationService.createNomination(request));
        }

        // First 40 valid nominations (in arrival order) are confirmed...
        List<NominationResponse> first40 = responses.subList(0, MAX_PARTICIPANTS);
        assertThat(first40).allSatisfy(r -> assertThat(r.status()).isEqualTo("CONFIRMED"));

        // ...and the remaining 20 are waitlisted.
        List<NominationResponse> remaining20 = responses.subList(MAX_PARTICIPANTS, TOTAL_NOMINATIONS);
        assertThat(remaining20).allSatisfy(r -> assertThat(r.status()).isEqualTo("WAITLISTED"));

        assertThat(nominationRepository.countByProgrammeIdAndStatus(programme.getId(), NominationStatus.CONFIRMED))
                .isEqualTo(40);
        assertThat(nominationRepository.countByProgrammeIdAndStatus(programme.getId(), NominationStatus.WAITLISTED))
                .isEqualTo(20);

        // The 41st nomination submitted is the oldest person on the waiting list.
        Long firstWaitlistedId = responses.get(MAX_PARTICIPANTS).id();

        // A confirmed participant cancels...
        Long cancelledId = responses.get(0).id();
        NominationResponse cancelled = nominationService.cancelNomination(cancelledId);
        assertThat(cancelled.status()).isEqualTo("CANCELLED");

        // ...and the oldest waitlisted nomination is automatically promoted.
        Nomination promoted = nominationRepository.findById(firstWaitlistedId).orElseThrow();
        assertThat(promoted.getStatus()).isEqualTo(NominationStatus.CONFIRMED);

        // Capacity is preserved: still exactly 40 confirmed, now 19 waitlisted.
        assertThat(nominationRepository.countByProgrammeIdAndStatus(programme.getId(), NominationStatus.CONFIRMED))
                .isEqualTo(40);
        assertThat(nominationRepository.countByProgrammeIdAndStatus(programme.getId(), NominationStatus.WAITLISTED))
                .isEqualTo(19);
    }
}
