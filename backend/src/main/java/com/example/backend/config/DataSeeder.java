package com.example.backend.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.backend.entity.Department;
import com.example.backend.entity.Officer;
import com.example.backend.entity.TrainingProgramme;
import com.example.backend.repository.DepartmentRepository;
import com.example.backend.repository.OfficerRepository;
import com.example.backend.repository.TrainingProgrammeRepository;

// Seeds a handful of departments, officers and one training programme on
// first run, purely so the frontend has real data to demonstrate the
// duplicate-nomination check against. Skipped if data already exists.
@Component
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final OfficerRepository officerRepository;
    private final TrainingProgrammeRepository programmeRepository;

    public DataSeeder(
            DepartmentRepository departmentRepository,
            OfficerRepository officerRepository,
            TrainingProgrammeRepository programmeRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.officerRepository = officerRepository;
        this.programmeRepository = programmeRepository;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            return;
        }

        Department finance = departmentRepository.save(new Department("Finance Division"));
        Department admin = departmentRepository.save(new Department("Administration Division"));
        Department hr = departmentRepository.save(new Department("Human Resources Division"));
        Department it = departmentRepository.save(new Department("IT Division"));
        Department procurement = departmentRepository.save(new Department("Procurement Division"));

        officerRepository.save(new Officer("EMP001", "A. Perera", finance));
        officerRepository.save(new Officer("EMP002", "S. Fernando", admin));
        officerRepository.save(new Officer("EMP003", "N. Silva", hr));
        officerRepository.save(new Officer("EMP004", "K. Jayawardena", it));
        officerRepository.save(new Officer("EMP005", "R. Wickramasinghe", procurement));
        officerRepository.save(new Officer("EMP006", "D. Gunasekara", finance));
        officerRepository.save(new Officer("EMP007", "M. Rathnayake", admin));
        officerRepository.save(new Officer("EMP008", "T. Bandara", hr));

        TrainingProgramme programme = new TrainingProgramme();
        programme.setTitle("Public Financial Management Workshop");
        programme.setTrainingDate(LocalDate.now().plusDays(14));
        programme.setVenue("Main Auditorium");
        programme.setTrainer("Dr. C. Amarasinghe");
        programme.setMaxParticipants(50);
        programmeRepository.save(programme);
    }
}
