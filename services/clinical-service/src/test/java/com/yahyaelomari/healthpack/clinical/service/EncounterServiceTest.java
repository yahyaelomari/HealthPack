package com.yahyaelomari.healthpack.clinical.service;

import com.yahyaelomari.healthpack.clinical.api.dto.OpenEncounterRequest;
import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.domain.EncounterStatus;
import com.yahyaelomari.healthpack.clinical.domain.EncounterType;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.mapper.EncounterMapper;
import com.yahyaelomari.healthpack.clinical.repository.EncounterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private EncounterMapper encounterMapper;

    @InjectMocks
    private EncounterService encounterService;

    @Test
    void openSavesAnInProgressEncounter() {
        encounterService.open(newOpenRequest());

        ArgumentCaptor<Encounter> saved = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
    }

    @Test
    void findByIdThrowsWhenTheEncounterDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(encounterRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> encounterService.findById(id))
                .isInstanceOf(EncounterNotFoundException.class);
    }

    @Test
    void finishClosesAnOpenEncounter() {
        UUID id = UUID.randomUUID();
        Encounter encounter = newEncounter();
        when(encounterRepository.findById(id)).thenReturn(Optional.of(encounter));

        encounterService.finish(id, Instant.now());

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.FINISHED);
    }

    @Test
    void finishThrowsWhenTheEncounterIsAlreadyFinished() {
        UUID id = UUID.randomUUID();
        Encounter encounter = newEncounter();
        encounter.finish(Instant.now());
        when(encounterRepository.findById(id)).thenReturn(Optional.of(encounter));

        assertThatThrownBy(() -> encounterService.finish(id, Instant.now()))
                .isInstanceOf(EncounterClosedException.class);
    }

    @Test
    void cancelThrowsWhenTheEncounterIsAlreadyFinished() {
        UUID id = UUID.randomUUID();
        Encounter encounter = newEncounter();
        encounter.finish(Instant.now());
        when(encounterRepository.findById(id)).thenReturn(Optional.of(encounter));

        assertThatThrownBy(() -> encounterService.cancel(id, "patient left"))
                .isInstanceOf(EncounterClosedException.class);
    }

    private OpenEncounterRequest newOpenRequest() {
        return new OpenEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                EncounterType.AMBULATORY,
                Instant.now().minusSeconds(60),
                "check-up");
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