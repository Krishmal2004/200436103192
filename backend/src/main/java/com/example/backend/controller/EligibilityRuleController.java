package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.EligibilityRuleRequest;
import com.example.backend.dto.EligibilityRuleResponse;
import com.example.backend.service.EligibilityService;

import jakarta.validation.Valid;

// Lets a coordinator configure which departments/grades/years-of-service/
// cooldown apply to a programme as data, without a code change or a
// redeploy. See docs/task03_workflow.md, section 5.
@RestController
@RequestMapping("/api/programmes/{programmeId}/eligibility-rules")
public class EligibilityRuleController {

    private final EligibilityService eligibilityService;

    public EligibilityRuleController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @GetMapping
    public List<EligibilityRuleResponse> list(@PathVariable Long programmeId) {
        return eligibilityService.listRules(programmeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EligibilityRuleResponse create(@PathVariable Long programmeId, @Valid @RequestBody EligibilityRuleRequest request) {
        return eligibilityService.addRule(programmeId, request);
    }

    @DeleteMapping("/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long programmeId, @PathVariable Long ruleId) {
        eligibilityService.deleteRule(programmeId, ruleId);
    }
}
