package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Nomination;
import com.example.backend.entity.NominationStatus;

public interface NominationRepository extends JpaRepository<Nomination, Long> {

    List<Nomination> findByProgrammeIdOrderByCreatedAtAsc(Long programmeId);

    Optional<Nomination> findByProgrammeIdAndOfficerId(Long programmeId, Long officerId);

    long countByProgrammeIdAndStatus(Long programmeId, NominationStatus status);

    // Oldest still-waitlisted nomination for a programme — the next person
    // to promote when a confirmed seat is freed up. See docs/task02_workflow.md.
    Optional<Nomination> findFirstByProgrammeIdAndStatusOrderByCreatedAtAsc(Long programmeId, NominationStatus status);
}
