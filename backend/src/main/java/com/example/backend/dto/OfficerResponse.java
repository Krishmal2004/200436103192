package com.example.backend.dto;

import java.time.LocalDate;

public record OfficerResponse(
        Long id,
        String employeeNo,
        String name,
        Long departmentId,
        String departmentName,
        String grade,
        LocalDate joinedDate
) {
}
