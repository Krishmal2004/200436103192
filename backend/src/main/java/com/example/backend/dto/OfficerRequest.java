package com.example.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OfficerRequest(
        @NotBlank String employeeNo,
        @NotBlank String name,
        @NotNull Long departmentId,
        // Both optional: needed only for programmes with a GRADE or
        // MIN_YEARS_OF_SERVICE eligibility rule. See docs/task03_workflow.md.
        String grade,
        LocalDate joinedDate
) {
}
