package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.NominationRequest;
import com.example.backend.dto.NominationResponse;
import com.example.backend.service.NominationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/nominations")
public class NominationController {

    private final NominationService nominationService;

    public NominationController(NominationService nominationService) {
        this.nominationService = nominationService;
    }

    @GetMapping
    public List<NominationResponse> getForProgramme(@RequestParam Long programmeId) {
        return nominationService.getNominationsForProgramme(programmeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NominationResponse create(@Valid @RequestBody NominationRequest request) {
        return nominationService.createNomination(request);
    }

    // Cancels a nomination; if it was CONFIRMED, the oldest WAITLISTED
    // nomination for the same programme is promoted automatically.
    // See docs/task02_workflow.md, section 6.
    @DeleteMapping("/{id}")
    public NominationResponse cancel(@PathVariable Long id) {
        return nominationService.cancelNomination(id);
    }
}
