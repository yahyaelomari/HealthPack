package com.yahyaelomari.healthpack.clinical.service;

import com.yahyaelomari.healthpack.clinical.api.dto.ObservationResponse;
import com.yahyaelomari.healthpack.clinical.api.dto.RecordObservationRequest;
import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.domain.Observation;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.mapper.ObservationMapper;
import com.yahyaelomari.healthpack.clinical.repository.EncounterRepository;
import com.yahyaelomari.healthpack.clinical.repository.ObservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ObservationService {

    private final ObservationRepository observationRepository;
    private final EncounterRepository encounterRepository;
    private final ObservationMapper observationMapper;

    public ObservationResponse record(UUID encounterId, RecordObservationRequest request) {
        requireOpenEncounter(encounterId);

        Observation observation = Observation.builder()
                .encounterId(encounterId)
                .code(request.code())
                .display(request.display())
                .value(request.value())
                .unit(request.unit())
                .recordedAt(request.recordedAt())
                .build();

        return observationMapper.toResponse(observationRepository.save(observation));
    }


    @Transactional(readOnly = true)
    public List<ObservationResponse> findByEncounterId(UUID encounterId) {
        if (!encounterRepository.existsById(encounterId)) {
            throw new EncounterNotFoundException(encounterId);
        }
        return observationRepository.findByEncounterId(encounterId).stream()
                .map(observationMapper::toResponse)
                .toList();
    }

    private void requireOpenEncounter(UUID encounterId) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));
        if (!encounter.isOpen()) {
            throw new EncounterClosedException(encounterId);
        }
    }
}
