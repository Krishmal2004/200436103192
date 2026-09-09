package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Officer;

public interface OfficerRepository extends JpaRepository<Officer, Long> {

    List<Officer> findByDepartmentId(Long departmentId);
}
