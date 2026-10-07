package com.yahyaelomari.healthpack.clinical.service;

import com.yahyaelomari.healthpack.clinical.api.dto.RecordObservationRequest;
import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.domain.EncounterType;
import com.yahyaelomari.healthpack.clinical.domain.Observation;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.mapper.ObservationMapper;
import com.yahyaelomari.healthpack.clinical.repository.EncounterRepository;
import com.yahyaelomari.healthpack.clinical.repository.ObservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The point of this class: nothing can be attached to an encounter that is
 * missing or already closed, and nothing is written when that happens.
 */
@ExtendWith(MockitoExtension.class)
class ObservationServiceTest {

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private ObservationMapper observationMapper;

    @InjectMocks
    private ObservationService observationService;

    @Test
    void recordSavesAnObservationOnAnOpenEncounter() {
        UUID encounterId = UUID.randomUUID();
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(newEncounter()));

        observationService.record(encounterId, newRequest());

        verify(observationRepository).save(any(Observation.class));
    }

    @Test
    void recordThrowsWhenTheEncounterDoesNotExist() {
        UUID encounterId = UUID.randomUUID();
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> observationService.record(encounterId, newRequest()))
                .isInstanceOf(EncounterNotFoundException.class);
        verify(observationRepository, never()).save(any());
    }

    @Test
    void recordThrowsWhenTheEncounterIsAlreadyFinished() {
        UUID encounterId = UUID.randomUUID();
        Encounter finished = newEncounter();
        finished.finish(Instant.now());
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(finished));

        assertThatThrownBy(() -> observationService.record(encounterId, newRequest()))
                .isInstanceOf(EncounterClosedException.class);
        verify(observationRepository, never()).save(any());
    }

    @Test
    void findByEncounterIdThrowsWhenTheEncounterDoesNotExist() {
        UUID encounterId = UUID.randomUUID();
        when(encounterRepository.existsById(encounterId)).thenReturn(false);

        assertThatThrownBy(() -> observationService.findByEncounterId(encounterId))
                .isInstanceOf(EncounterNotFoundException.class);
    }

    private RecordObservationRequest newRequest() {
        return new RecordObservationRequest(
                "8867-4", "Heart rate", new BigDecimal("72"), "beats/min", Instant.now().minusSeconds(30));
    }

    private Encounter newEncounter() {
        return Encounter.builder()
                .patientId(UUID.randomUUID())
                .practitionerId(UUID.randomUUID())
                .type(EncounterType.AMBULATORY)
                .startedAt(Instant.now().minusSeconds(600))
                .build();
    }
}