package com.yahyaelomari.healthpack.scheduling.repository;

import com.yahyaelomari.healthpack.scheduling.domain.Practitioner;
import com.yahyaelomari.healthpack.scheduling.domain.Specialty;
import com.yahyaelomari.healthpack.scheduling.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PractitionerRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PractitionerRepository practitionerRepository;

    @Test
    void duplicateNpiIsRejectedByTheDatabase() {
        practitionerRepository.saveAndFlush(newPractitioner("1234567890"));

        assertThatThrownBy(() -> practitionerRepository.saveAndFlush(newPractitioner("1234567890")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByNpiReturnsTheMatchingPractitioner() {
        practitionerRepository.saveAndFlush(newPractitioner("9999999999"));

        assertThat(practitionerRepository.findByNpi("9999999999"))
                .isPresent()
                .get()
                .extracting(Practitioner::getFullName)
                .isEqualTo("Dr. Jamie Doe");
    }

    private Practitioner newPractitioner(String npi) {
        return Practitioner.builder()
                .npi(npi)
                .fullName("Dr. Jamie Doe")
                .specialty(Specialty.GENERAL_PRACTICE)
                .build();
    }
}
