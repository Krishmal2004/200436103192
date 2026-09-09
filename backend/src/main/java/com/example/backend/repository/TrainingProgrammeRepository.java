package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.example.backend.entity.TrainingProgramme;

public interface TrainingProgrammeRepository extends JpaRepository<TrainingProgramme, Long> {

    // Row-locking read used while deciding CONFIRMED vs WAITLISTED (and while
    // cancelling+promoting), so two requests racing for the same programme's
    // last seat are serialized instead of both reading the same confirmed
    // count and both landing as CONFIRMED. See docs/task02_workflow.md, section 5.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from TrainingProgramme p where p.id = :id")
    Optional<TrainingProgramme> findByIdForUpdate(@Param("id") Long id);
}
