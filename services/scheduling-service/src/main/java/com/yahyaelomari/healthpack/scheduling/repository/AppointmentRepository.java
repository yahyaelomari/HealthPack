package com.yahyaelomari.healthpack.scheduling.repository;

import com.yahyaelomari.healthpack.scheduling.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatientId(UUID patientId);

    List<Appointment> findByPractitionerIdAndSlotStartBetween(UUID practitionerId, Instant from, Instant to);
}
