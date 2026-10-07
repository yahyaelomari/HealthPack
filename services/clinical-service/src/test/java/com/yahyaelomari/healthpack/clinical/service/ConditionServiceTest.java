package com.yahyaelomari.healthpack.clinical.service;

import com.yahyaelomari.healthpack.clinical.api.dto.DiagnoseRequest;
import com.yahyaelomari.healthpack.clinical.domain.ClinicalStatus;
import com.yahyaelomari.healthpack.clinical.domain.Condition;
import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.domain.EncounterType;
import com.yahyaelomari.healthpack.clinical.exception.ConditionNotFoundException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.mapper.ConditionMapper;
import com.yahyaelomari.healthpack.clinical.repository.ConditionRepository;
import com.yahyaelomari.healthpack.clinical.repository.EncounterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConditionServiceTest {

    @Mock
    private ConditionRepository conditionRepository;

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private ConditionMapper conditionMapper;

    @InjectMocks
    private ConditionService conditionService;

    @Test
    void diagnoseSavesAConditionOnAnOpenEncounter() {
        UUID encounterId = UUID.randomUUID();
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(newEncounter()));

        conditionService.diagnose(encounterId, newRequest());

        verify(conditionRepository).save(any(Condition.class));
    }

    @Test
    void diagnoseThrowsWhenTheEncounterDoesNotExist() {
        UUID encounterId = UUID.randomUUID();
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conditionService.diagnose(encounterId, newRequest()))
                .isInstanceOf(EncounterNotFoundException.class);
        verify(conditionRepository, never()).save(any());
    }

    @Test
    void diagnoseThrowsWhenTheEncounterIsAlreadyFinished() {
        UUID encounterId = UUID.randomUUID();
        Encounter finished = newEncounter();
        finished.finish(Instant.now());
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(finished));

        assertThatThrownBy(() -> conditionService.diagnose(encounterId, newRequest()))
                .isInstanceOf(EncounterClosedException.class);
        verify(conditionRepository, never()).save(any());
    }

    @Test
    void resolveMarksTheConditionResolved() {
        UUID id = UUID.randomUUID();
        Condition condition = Condition.builder()
                .encounterId(UUID.randomUUID())
                .code("I10")
                .display("Essential hypertension")
                .recordedAt(Instant.now())
                .build();
        when(conditionRepository.findById(id)).thenReturn(Optional.of(condition));

        conditionService.resolve(id);

        assertThat(condition.getClinicalStatus()).isEqualTo(ClinicalStatus.RESOLVED);
    }

    @Test
    void resolveThrowsWhenTheConditionDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(conditionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conditionService.resolve(id))
                .isInstanceOf(ConditionNotFoundException.class);
    }

    private DiagnoseRequest newRequest() {
        return new DiagnoseRequest("I10", "Essential hypertension", null, Instant.now().minusSeconds(30));
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