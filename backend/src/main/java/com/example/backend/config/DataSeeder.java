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

        // grade + joinedDate filled in so Task 3's GRADE / MIN_YEARS_OF_SERVICE
        // eligibility rules have real data to evaluate against out of the box.
        officerRepository.save(officer("EMP001", "A. Perera", finance, "Officer", LocalDate.now().minusYears(2)));
        officerRepository.save(officer("EMP002", "S. Fernando", admin, "Senior Officer", LocalDate.now().minusYears(6)));
        officerRepository.save(officer("EMP003", "N. Silva", hr, "Officer", LocalDate.now().minusYears(1)));
        officerRepository.save(officer("EMP004", "K. Jayawardena", it, "Senior Officer", LocalDate.now().minusYears(8)));
        officerRepository.save(officer("EMP005", "R. Wickramasinghe", procurement, "Officer", LocalDate.now().minusYears(3)));
        officerRepository.save(officer("EMP006", "D. Gunasekara", finance, "Director", LocalDate.now().minusYears(12)));
        officerRepository.save(officer("EMP007", "M. Rathnayake", admin, "Officer", LocalDate.now().minusMonths(6)));
        officerRepository.save(officer("EMP008", "T. Bandara", hr, "Senior Officer", LocalDate.now().minusYears(5)));

        TrainingProgramme programme = new TrainingProgramme();
        programme.setTitle("Public Financial Management Workshop");
        programme.setTrainingDate(LocalDate.now().plusDays(14));
        programme.setVenue("Main Auditorium");
        programme.setTrainer("Dr. C. Amarasinghe");
        programme.setMaxParticipants(50);
        programme.setProgrammeCode("PUB-FIN-MGMT");
        programmeRepository.save(programme);
    }

    private Officer officer(String employeeNo, String name, Department department, String grade, LocalDate joinedDate) {
        Officer officer = new Officer(employeeNo, name, department);
        officer.setGrade(grade);
        officer.setJoinedDate(joinedDate);
        return officer;
    }
}
