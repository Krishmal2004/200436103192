package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OfficerRequest(
        @NotBlank String employeeNo,
        @NotBlank String name,
        @NotNull Long departmentId
) {
}
