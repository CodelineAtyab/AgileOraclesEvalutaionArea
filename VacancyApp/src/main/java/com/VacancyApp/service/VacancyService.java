package com.VacancyApp.service;

import com.VacancyApp.exception.ConflictException;
import com.VacancyApp.exception.NotFoundException;
import com.VacancyApp.exception.ValidationException;
import com.VacancyApp.model.CreateVacancyRequest;
import com.VacancyApp.model.Vacancy;
import com.VacancyApp.model.VacancyApplication;
import com.VacancyApp.repository.VacancyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Application-facing vacancy operations. Business logic (expiry, normalization, etc.)
 * stays in Oracle; this service only reads via procedures and performs the two simple writes.
 */
@Service
public class VacancyService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final VacancyRepository vacancyRepository;

    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    /** Currently available vacancies (OPEN and not expired) — straight from Oracle. */
    public List<Vacancy> getAvailableVacancies() {
        return vacancyRepository.findAvailable();
    }

    /** HR creates a vacancy. Returns the newly created row. */
    public Vacancy createVacancy(CreateVacancyRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        String title = trimToNull(request.title());
        String description = trimToNull(request.description());
        String department = trimToNull(request.department());
        LocalDateTime expiresAt = request.expiresAt();

        if (title == null) {
            throw new ValidationException("title is required");
        }
        if (description == null) {
            throw new ValidationException("description is required");
        }
        if (expiresAt == null) {
            throw new ValidationException("expiresAt is required");
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new ValidationException("expiresAt must be in the future");
        }

        long jobId = vacancyRepository.createVacancy(title, description, department, expiresAt);
        return vacancyRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Created vacancy " + jobId + " could not be read back"));
    }

    /** A candidate applies to a vacancy. Validates the job is open and not expired. */
    public VacancyApplication apply(long jobId, String applicantName, String applicantEmail) {
        String name = trimToNull(applicantName);
        String email = trimToNull(applicantEmail);
        if (name == null) {
            throw new ValidationException("applicantName is required");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new ValidationException("a valid applicantEmail is required");
        }

        Vacancy vacancy = vacancyRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Vacancy " + jobId + " does not exist"));

        boolean open = "OPEN".equalsIgnoreCase(vacancy.status());
        boolean notExpired = vacancy.expiresAt() != null && vacancy.expiresAt().isAfter(LocalDateTime.now());
        if (!open || !notExpired) {
            throw new ConflictException("Vacancy " + jobId + " is not open for applications");
        }

        return vacancyRepository.insertApplication(jobId, name, email);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
