package com.example.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "nominations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_officer_programme",
                columnNames = {"officer_id", "programme_id"}
        )
)
public class Nomination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private TrainingProgramme programme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "officer_id", nullable = false)
    private Officer officer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nominated_by_department_id", nullable = false)
    private Department nominatedByDepartment;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NominationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingProgramme getProgramme() {
        return programme;
    }

    public void setProgramme(TrainingProgramme programme) {
        this.programme = programme;
    }

    public Officer getOfficer() {
        return officer;
    }

    public void setOfficer(Officer officer) {
        this.officer = officer;
    }

    public Department getNominatedByDepartment() {
        return nominatedByDepartment;
    }

    public void setNominatedByDepartment(Department nominatedByDepartment) {
        this.nominatedByDepartment = nominatedByDepartment;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public NominationStatus getStatus() {
        return status;
    }

    public void setStatus(NominationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
