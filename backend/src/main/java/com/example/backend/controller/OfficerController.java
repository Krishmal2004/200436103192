package com.example.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.OfficerResponse;
import com.example.backend.entity.Officer;
import com.example.backend.repository.OfficerRepository;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    private final OfficerRepository officerRepository;

    public OfficerController(OfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    @GetMapping
    public List<OfficerResponse> getAll(@RequestParam(required = false) Long departmentId) {
        List<Officer> officers = departmentId != null
                ? officerRepository.findByDepartmentId(departmentId)
                : officerRepository.findAll();
        return officers.stream().map(this::toResponse).toList();
    }

    private OfficerResponse toResponse(Officer o) {
        return new OfficerResponse(
                o.getId(),
                o.getEmployeeNo(),
                o.getName(),
                o.getDepartment().getId(),
                o.getDepartment().getName()
        );
    }
}
