package com.yahyaelomari.healthpack.patient.repository;

import com.yahyaelomari.healthpack.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link Patient}.
 *
 * <p>{@link JpaSpecificationExecutor} is what lets {@link PatientSpecifications}
 * compose filters at query time instead of the repository growing one
 * {@code findByXAndYAndZ} method per combination of search fields.
 */
public interface PatientRepository extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {

    Optional<Patient> findByMrn(String mrn);

    boolean existsByMrn(String mrn);
}
