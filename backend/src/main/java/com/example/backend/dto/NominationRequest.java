package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public record NominationRequest(
        @NotNull Long programmeId,
        @NotNull Long officerId,
        @NotNull Long departmentId,
        String submittedBy
) {
}
