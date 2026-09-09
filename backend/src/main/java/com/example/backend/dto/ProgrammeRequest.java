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
        @NotNull @Min(1) Integer maxParticipants
) {
}
