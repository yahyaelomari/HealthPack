package com.yahyaelomari.healthpack.clinical.repository;

import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.domain.EncounterStatus;
import com.yahyaelomari.healthpack.clinical.domain.EncounterType;
import com.yahyaelomari.healthpack.clinical.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EncounterRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private EncounterRepository encounterRepository;

    @Test
    void findByPatientIdReturnsOnlyThatPatientsEncounters() {
        UUID patientId = UUID.randomUUID();
        encounterRepository.saveAndFlush(newEncounter(patientId));
        encounterRepository.saveAndFlush(newEncounter(UUID.randomUUID()));

        assertThat(encounterRepository.findByPatientId(patientId)).hasSize(1);
    }

    @Test
    void findByPatientIdAndStatusSeparatesOpenFromFinished() {
        UUID patientId = UUID.randomUUID();
        encounterRepository.saveAndFlush(newEncounter(patientId));

        Encounter finished = newEncounter(patientId);
        finished.finish(Instant.now());
        encounterRepository.saveAndFlush(finished);

        assertThat(encounterRepository.findByPatientIdAndStatus(patientId, EncounterStatus.IN_PROGRESS)).hasSize(1);
        assertThat(encounterRepository.findByPatientIdAndStatus(patientId, EncounterStatus.FINISHED)).hasSize(1);
    }

    @Test
    void versionIsIncrementedOnUpdate() {
        Encounter encounter = encounterRepository.saveAndFlush(newEncounter(UUID.randomUUID()));
        long initialVersion = encounter.getVersion();

        encounter.finish(Instant.now());
        Encounter updated = encounterRepository.saveAndFlush(encounter);

        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
    }

    private Encounter newEncounter(UUID patientId) {
        return Encounter.builder()
                .patientId(patientId)
                .practitionerId(UUID.randomUUID())
                .type(EncounterType.AMBULATORY)
                .startedAt(Instant.now().minusSeconds(600))
                .reason("annual check-up")
                .build();
    }
}
