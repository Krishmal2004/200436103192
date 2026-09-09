package com.example.backend.dto;

import java.time.LocalDate;

public record ProgrammeResponse(
        Long id,
        String title,
        LocalDate trainingDate,
        String venue,
        String trainer,
        Integer maxParticipants,
        long confirmedCount
) {
}
