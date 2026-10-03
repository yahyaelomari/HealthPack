package com.yahyaelomari.healthpack.clinical.repository;

import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.domain.EncounterStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EncounterRepository extends JpaRepository<Encounter, UUID> {

    List<Encounter> findByPatientId(UUID patientId);

    List<Encounter> findByPatientIdAndStatus(UUID patientId, EncounterStatus status);
}
