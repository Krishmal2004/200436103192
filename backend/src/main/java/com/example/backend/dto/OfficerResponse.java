package com.example.backend.dto;

public record OfficerResponse(
        Long id,
        String employeeNo,
        String name,
        Long departmentId,
        String departmentName
) {
}
