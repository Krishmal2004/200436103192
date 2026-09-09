package com.example.backend.service;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.NominationRequest;
import com.example.backend.dto.NominationResponse;
import com.example.backend.entity.Department;
import com.example.backend.entity.Nomination;
import com.example.backend.entity.NominationStatus;
import com.example.backend.entity.Officer;
import com.example.backend.entity.TrainingProgramme;
import com.example.backend.exception.DuplicateNominationException;
import com.example.backend.exception.InvalidNominationStateException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.DepartmentRepository;
import com.example.backend.repository.NominationRepository;
import com.example.backend.repository.OfficerRepository;
import com.example.backend.repository.TrainingProgrammeRepository;

@Service
public class NominationService {

    private final NominationRepository nominationRepository;
    private final OfficerRepository officerRepository;
    private final TrainingProgrammeRepository programmeRepository;
    private final DepartmentRepository departmentRepository;

    public NominationService(
            NominationRepository nominationRepository,
            OfficerRepository officerRepository,
            TrainingProgrammeRepository programmeRepository,
            DepartmentRepository departmentRepository
    ) {
        this.nominationRepository = nominationRepository;
        this.officerRepository = officerRepository;
        this.programmeRepository = programmeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public NominationResponse createNomination(NominationRequest request) {
        Officer officer = officerRepository.findById(request.officerId())
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found: " + request.officerId()));
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.departmentId()));

        // Lock the programme row before deciding CONFIRMED vs WAITLISTED, so two
        // nominations racing for the same last seat are serialized instead of both
        // reading the same confirmed count and both landing as CONFIRMED.
        // See docs/task02_workflow.md, section 5.
        TrainingProgramme programme = programmeRepository.findByIdForUpdate(request.programmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Training programme not found: " + request.programmeId()));

        // Application-level duplicate check: fast, and gives a friendly message.
        nominationRepository.findByProgrammeIdAndOfficerId(programme.getId(), officer.getId())
                .ifPresent(existing -> {
                    throw new DuplicateNominationException(
                            officer.getName(),
                            existing.getNominatedByDepartment().getName(),
                            existing.getCreatedAt()
                    );
                });

        long confirmedCount = nominationRepository.countByProgrammeIdAndStatus(programme.getId(), NominationStatus.CONFIRMED);
        NominationStatus status = confirmedCount < programme.getMaxParticipants()
                ? NominationStatus.CONFIRMED
                : NominationStatus.WAITLISTED;

        Nomination nomination = new Nomination();
        nomination.setProgramme(programme);
        nomination.setOfficer(officer);
        nomination.setNominatedByDepartment(department);
        nomination.setSubmittedBy(request.submittedBy());
        nomination.setStatus(status);
        nomination.setCreatedAt(Instant.now());

        Nomination saved;
        try {
            saved = nominationRepository.save(nomination);
        } catch (DataIntegrityViolationException e) {
            // Database-level backstop: two requests raced past the check above
            throw new DuplicateNominationException(officer.getName(), "another department", Instant.now());
        }

        return toResponse(saved);
    }

    @Transactional
    public NominationResponse cancelNomination(Long nominationId) {
        Nomination nomination = nominationRepository.findById(nominationId)
                .orElseThrow(() -> new ResourceNotFoundException("Nomination not found: " + nominationId));

        if (nomination.getStatus() == NominationStatus.CANCELLED) {
            throw new InvalidNominationStateException("Nomination is already cancelled.");
        }

        boolean freedAConfirmedSeat = nomination.getStatus() == NominationStatus.CONFIRMED;
        Long programmeId = nomination.getProgramme().getId();

        // Lock the programme row for the same reason createNomination does: this
        // cancel-then-promote must not interleave with a concurrent nomination or
        // cancellation on the same programme. See docs/task02_workflow.md, section 6.
        programmeRepository.findByIdForUpdate(programmeId)
                .orElseThrow(() -> new ResourceNotFoundException("Training programme not found: " + programmeId));

        nomination.setStatus(NominationStatus.CANCELLED);
        nominationRepository.save(nomination);

        if (freedAConfirmedSeat) {
            // Promote the oldest still-waitlisted nomination into the seat that just opened up.
            nominationRepository
                    .findFirstByProgrammeIdAndStatusOrderByCreatedAtAsc(programmeId, NominationStatus.WAITLISTED)
                    .ifPresent(next -> {
                        next.setStatus(NominationStatus.CONFIRMED);
                        nominationRepository.save(next);
                    });
        }

        return toResponse(nomination);
    }

    @Transactional(readOnly = true)
    public List<NominationResponse> getNominationsForProgramme(Long programmeId) {
        return nominationRepository.findByProgrammeIdOrderByCreatedAtAsc(programmeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NominationResponse toResponse(Nomination n) {
        return new NominationResponse(
                n.getId(),
                n.getProgramme().getId(),
                n.getProgramme().getTitle(),
                n.getOfficer().getId(),
                n.getOfficer().getName(),
                n.getOfficer().getEmployeeNo(),
                n.getNominatedByDepartment().getId(),
                n.getNominatedByDepartment().getName(),
                n.getSubmittedBy(),
                n.getStatus().name(),
                n.getCreatedAt()
        );
    }
}
