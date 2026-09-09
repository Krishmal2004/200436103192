package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ProgrammeRequest;
import com.example.backend.dto.ProgrammeResponse;
import com.example.backend.entity.NominationStatus;
import com.example.backend.entity.TrainingProgramme;
import com.example.backend.repository.NominationRepository;
import com.example.backend.repository.TrainingProgrammeRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/programmes")
public class ProgrammeController {

    private final TrainingProgrammeRepository programmeRepository;
    private final NominationRepository nominationRepository;

    public ProgrammeController(TrainingProgrammeRepository programmeRepository, NominationRepository nominationRepository) {
        this.programmeRepository = programmeRepository;
        this.nominationRepository = nominationRepository;
    }

    @GetMapping
    public List<ProgrammeResponse> getAll() {
        return programmeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProgrammeResponse create(@Valid @RequestBody ProgrammeRequest request) {
        TrainingProgramme programme = new TrainingProgramme();
        programme.setTitle(request.title());
        programme.setTrainingDate(request.trainingDate());
        programme.setVenue(request.venue());
        programme.setTrainer(request.trainer());
        programme.setMaxParticipants(request.maxParticipants());
        return toResponse(programmeRepository.save(programme));
    }

    private ProgrammeResponse toResponse(TrainingProgramme p) {
        long confirmed = nominationRepository.countByProgrammeIdAndStatus(p.getId(), NominationStatus.CONFIRMED);
        return new ProgrammeResponse(
                p.getId(),
                p.getTitle(),
                p.getTrainingDate(),
                p.getVenue(),
                p.getTrainer(),
                p.getMaxParticipants(),
                confirmed
        );
    }
}
