package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
