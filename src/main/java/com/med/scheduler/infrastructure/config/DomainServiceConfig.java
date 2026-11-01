package com.med.scheduler.infrastructure.config;

import com.med.scheduler.domain.repository.AppointmentRepositoryPort;
import com.med.scheduler.domain.repository.DoctorRepositoryPort;
import com.med.scheduler.domain.service.AppointmentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public AppointmentDomainService appointmentDomainService(
            DoctorRepositoryPort doctorRepositoryPort,
            AppointmentRepositoryPort appointmentRepositoryPort
    ) {
        return new AppointmentDomainService(doctorRepositoryPort, appointmentRepositoryPort);
    }
}
