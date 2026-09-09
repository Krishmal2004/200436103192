package com.example.backend.exception;

import java.time.Instant;


public class DuplicateNominationException extends RuntimeException {

    private final String existingDepartmentName;
    private final Instant existingNominatedAt;

    public DuplicateNominationException(String officerName, String existingDepartmentName, Instant existingNominatedAt) {
        super("Officer '" + officerName + "' was already nominated for this programme by "
                + existingDepartmentName + " on " + existingNominatedAt);
        this.existingDepartmentName = existingDepartmentName;
        this.existingNominatedAt = existingNominatedAt;
    }

    public String getExistingDepartmentName() {
        return existingDepartmentName;
    }

    public Instant getExistingNominatedAt() {
        return existingNominatedAt;
    }
}
