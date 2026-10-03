package com.yahyaelomari.healthpack.clinical.repository;

import com.yahyaelomari.healthpack.clinical.domain.Observation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ObservationRepository extends JpaRepository<Observation, UUID> {

    List<Observation> findByEncounterId(UUID encounterId);
}
