package com.VacancyApp.controller;

import com.VacancyApp.model.ApplyRequest;
import com.VacancyApp.model.CreateVacancyRequest;
import com.VacancyApp.model.Vacancy;
import com.VacancyApp.model.VacancyApplication;
import com.VacancyApp.service.VacancyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API for the vacancy board. */
@RestController
@RequestMapping("/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    /** Only currently available vacancies (OPEN and not expired). */
    @GetMapping("/available")
    public List<Vacancy> available() {
        return vacancyService.getAvailableVacancies();
    }

    /** HR creates a new vacancy. */
    @PostMapping
    public ResponseEntity<Vacancy> create(@RequestBody CreateVacancyRequest request) {
        Vacancy created = vacancyService.createVacancy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** A candidate applies to a vacancy. */
    @PostMapping("/{jobId}/applications")
    public ResponseEntity<VacancyApplication> apply(@PathVariable long jobId,
                                                    @RequestBody ApplyRequest request) {
        VacancyApplication application =
                vacancyService.apply(jobId, request.applicantName(), request.applicantEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }
}
