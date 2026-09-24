package com.yahyaelomari.healthpack.patient.repository;

import com.yahyaelomari.healthpack.patient.domain.ContactInfo;
import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.Patient;
import com.yahyaelomari.healthpack.patient.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * As of Spring Boot 4, {@code @DataJpaTest} no longer replaces the configured
 * {@code DataSource} with an embedded one — {@code @AutoConfigureTestDatabase}
 * was removed along with that behaviour. The Testcontainers Postgres wired by
 * {@link AbstractIntegrationTest}'s {@code @ServiceConnection} is used as-is.
 */
@DataJpaTest
class PatientRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void duplicateMrnIsRejectedByTheDatabase() {
        patientRepository.saveAndFlush(newPatient("P-0001"));

        // uk_patient_mrn from V1__patient.sql — the real backstop, the
        // application layer just gets to fail fast before hitting it.
        assertThatThrownBy(() -> patientRepository.saveAndFlush(newPatient("P-0001")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByMrnReturnsTheMatchingPatient() {
        patientRepository.saveAndFlush(newPatient("P-0002"));

        assertThat(patientRepository.findByMrn("P-0002"))
                .isPresent()
                .get()
                .extracting(Patient::getLastName)
                .isEqualTo("Doe");

        assertThat(patientRepository.findByMrn("no-such-mrn")).isEmpty();
    }

    @Test
    void existsByMrnReflectsWhatWasSaved() {
        patientRepository.saveAndFlush(newPatient("P-0003"));

        assertThat(patientRepository.existsByMrn("P-0003")).isTrue();
        assertThat(patientRepository.existsByMrn("P-9999")).isFalse();
    }

    @Test
    void concurrentUpdatesAreRejectedByOptimisticLocking() {
        Patient saved = patientRepository.saveAndFlush(newPatient("P-0004"));
        UUID id = saved.getId();

        // A single EntityManager only ever holds one instance per id (the
        // first-level cache), so simulating "two clinicians loaded the same
        // record" requires detaching between loads — otherwise the second
        // findById would just return the exact same Java object as the first.
        entityManager.clear();

        Patient firstReader = patientRepository.findById(id).orElseThrow();
        entityManager.detach(firstReader);

        Patient secondReader = patientRepository.findById(id).orElseThrow();

        secondReader.updateDemographics("Jane", "Doe", secondReader.getBirthDate(), secondReader.getGender());
        patientRepository.saveAndFlush(secondReader);

        // firstReader still thinks the version is the one it read before
        // secondReader's write landed — exactly the case @Version exists to catch.
        firstReader.updateDemographics("Janet", "Doe", firstReader.getBirthDate(), firstReader.getGender());
        assertThatThrownBy(() -> patientRepository.saveAndFlush(firstReader))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    private static Patient newPatient(String mrn) {
        return Patient.builder()
                .mrn(mrn)
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .contact(ContactInfo.builder().email("john.doe@example.com").build())
                .build();
    }
}
