package com.med.scheduler.infrastructure.config;

import com.med.scheduler.domain.model.Doctor;
import com.med.scheduler.domain.repository.DoctorRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initSampleDoctors(DoctorRepositoryPort doctorRepository) {
        return args -> {
            if (doctorRepository.findByNameIgnoreCase("Dr. Alício").isEmpty()) {
                doctorRepository.save(new Doctor("doc-1", "Dr. Alício", "Cardiologia"));
            }
            if (doctorRepository.findByNameIgnoreCase("Dr. Roberto").isEmpty()) {
                doctorRepository.save(new Doctor("doc-2", "Dr. Roberto", "Ortopedia"));
            }
            if (doctorRepository.findByNameIgnoreCase("Dra. Beatriz Santos").isEmpty()) {
                doctorRepository.save(new Doctor("doc-3", "Dra. Beatriz Santos", "Dermatologia"));
            }
        };
    }
}
