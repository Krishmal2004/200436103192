package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.OfficerRequest;
import com.example.backend.dto.OfficerResponse;
import com.example.backend.entity.Department;
import com.example.backend.entity.Officer;
import com.example.backend.exception.DuplicateEmployeeNoException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.DepartmentRepository;
import com.example.backend.repository.OfficerRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    private final OfficerRepository officerRepository;
    private final DepartmentRepository departmentRepository;

    public OfficerController(OfficerRepository officerRepository, DepartmentRepository departmentRepository) {
        this.officerRepository = officerRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public List<OfficerResponse> getAll(@RequestParam(required = false) Long departmentId) {
        List<Officer> officers = departmentId != null
                ? officerRepository.findByDepartmentId(departmentId)
                : officerRepository.findAll();
        return officers.stream().map(this::toResponse).toList();
    }

    // Onboards a new officer onto the master list so they can be nominated.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfficerResponse create(@Valid @RequestBody OfficerRequest request) {
        officerRepository.findByEmployeeNo(request.employeeNo())
                .ifPresent(existing -> {
                    throw new DuplicateEmployeeNoException(request.employeeNo());
                });

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.departmentId()));

        Officer officer = new Officer(request.employeeNo(), request.name(), department);
        officer.setGrade(request.grade());
        officer.setJoinedDate(request.joinedDate());
        return toResponse(officerRepository.save(officer));
    }

    private OfficerResponse toResponse(Officer o) {
        return new OfficerResponse(
                o.getId(),
                o.getEmployeeNo(),
                o.getName(),
                o.getDepartment().getId(),
                o.getDepartment().getName(),
                o.getGrade(),
                o.getJoinedDate()
        );
    }
}
