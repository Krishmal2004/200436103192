package com.example.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProgrammeRequest(
        @NotBlank String title,
        @NotNull @FutureOrPresent(message = "Training date must be today or a future date") LocalDate trainingDate,
        String venue,
        String trainer,
        @NotNull @Min(1) Integer maxParticipants,
        // Optional: identifies this session as the same recurring programme
        // as other sessions, for the 12-month cooldown rule (defaults to
        // `title` if left blank). See docs/task03_workflow.md, section 3.
        String programmeCode
) {
}
