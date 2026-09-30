package com.yahyaelomari.healthpack.scheduling.repository;

import com.yahyaelomari.healthpack.scheduling.domain.Practitioner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PractitionerRepository extends JpaRepository<Practitioner, UUID> {

    Optional<Practitioner> findByNpi(String npi);
}
