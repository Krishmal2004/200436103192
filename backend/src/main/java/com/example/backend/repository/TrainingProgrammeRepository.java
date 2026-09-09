package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.TrainingProgramme;

public interface TrainingProgrammeRepository extends JpaRepository<TrainingProgramme, Long> {
}
