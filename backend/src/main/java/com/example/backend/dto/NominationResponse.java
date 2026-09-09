package com.example.backend.dto;

import java.time.Instant;

public record NominationResponse(
        Long id,
        Long programmeId,
        String programmeTitle,
        Long officerId,
        String officerName,
        String employeeNo,
        Long departmentId,
        String departmentName,
        String submittedBy,
        String status,
        Instant createdAt
) {
}
