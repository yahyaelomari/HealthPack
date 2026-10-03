package com.yahyaelomari.healthpack.clinical.repository;

import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.domain.EncounterType;
import com.yahyaelomari.healthpack.clinical.domain.Observation;
import com.yahyaelomari.healthpack.clinical.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ObservationRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Test
    void findByEncounterIdReturnsOnlyThatEncountersObservations() {
        Encounter encounter = encounterRepository.saveAndFlush(newEncounter());
        Encounter other = encounterRepository.saveAndFlush(newEncounter());
        observationRepository.saveAndFlush(newObservation(encounter.getId()));
        observationRepository.saveAndFlush(newObservation(other.getId()));

        assertThat(observationRepository.findByEncounterId(encounter.getId())).hasSize(1);
    }

    @Test
    void anObservationCannotPointAtAnEncounterThatDoesNotExist() {
        assertThatThrownBy(() -> observationRepository.saveAndFlush(newObservation(UUID.randomUUID())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void valueKeepsItsScaleThroughTheDatabase() {
        Encounter encounter = encounterRepository.saveAndFlush(newEncounter());

        Observation saved = observationRepository.saveAndFlush(Observation.builder()
                .encounterId(encounter.getId())
                .code("29463-7")
                .display("Body weight")
                .value(new BigDecimal("72.500"))
                .unit("kg")
                .recordedAt(Instant.now())
                .build());

        assertThat(observationRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Observation::getValue)
                .isEqualTo(new BigDecimal("72.500"));
    }

    private Encounter newEncounter() {
        return Encounter.builder()
                .patientId(UUID.randomUUID())
                .practitionerId(UUID.randomUUID())
                .type(EncounterType.AMBULATORY)
                .startedAt(Instant.now().minusSeconds(600))
                .build();
    }

    private Observation newObservation(UUID encounterId) {
        return Observation.builder()
                .encounterId(encounterId)
                .code("8867-4")
                .display("Heart rate")
                .value(new BigDecimal("72.000"))
                .unit("beats/min")
                .recordedAt(Instant.now())
                .build();
    }
}
