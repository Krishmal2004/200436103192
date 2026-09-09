package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.ProgrammeEligibilityRule;

public interface ProgrammeEligibilityRuleRepository extends JpaRepository<ProgrammeEligibilityRule, Long> {

    List<ProgrammeEligibilityRule> findByProgrammeId(Long programmeId);
}
