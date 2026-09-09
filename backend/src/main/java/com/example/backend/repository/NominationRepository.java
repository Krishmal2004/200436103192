package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Nomination;
import com.example.backend.entity.NominationStatus;

public interface NominationRepository extends JpaRepository<Nomination, Long> {

    List<Nomination> findByProgrammeIdOrderByCreatedAtAsc(Long programmeId);

    // Every nomination across every programme — the government office's
    // full participant register, not filtered to one programme.
    List<Nomination> findAllByOrderByCreatedAtAsc();

    Optional<Nomination> findByProgrammeIdAndOfficerId(Long programmeId, Long officerId);

    long countByProgrammeIdAndStatus(Long programmeId, NominationStatus status);

    // Oldest still-waitlisted nomination for a programme — the next person
    // to promote when a confirmed seat is freed up. See docs/task02_workflow.md.
    Optional<Nomination> findFirstByProgrammeIdAndStatusOrderByCreatedAtAsc(Long programmeId, NominationStatus status);

    // Most recent confirmed (i.e. actually attended) session of "the same"
    // programme for this officer, across every scheduled session sharing a
    // programme_code — the 12-month cooldown check reads the winner's
    // session date. See docs/task03_workflow.md, section 4.
    Optional<Nomination> findFirstByOfficer_IdAndProgramme_ProgrammeCodeAndStatusOrderByProgramme_TrainingDateDesc(
            Long officerId, String programmeCode, NominationStatus status);
}
