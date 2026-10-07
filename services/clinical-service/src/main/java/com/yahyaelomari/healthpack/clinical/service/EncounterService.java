package com.yahyaelomari.healthpack.clinical.service;

import com.yahyaelomari.healthpack.clinical.api.dto.EncounterResponse;
import com.yahyaelomari.healthpack.clinical.api.dto.OpenEncounterRequest;
import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.mapper.EncounterMapper;
import com.yahyaelomari.healthpack.clinical.repository.EncounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final EncounterMapper encounterMapper;

    public EncounterResponse open(OpenEncounterRequest request) {
        Encounter encounter = Encounter.builder()
                .patientId(request.patientId())
                .practitionerId(request.practitionerId())
                .appointmentId(request.appointmentId())
                .type(request.type())
                .startedAt(request.startedAt())
                .reason(request.reason())
                .build();

        return encounterMapper.toResponse(encounterRepository.save(encounter));
    }

    @Transactional(readOnly = true)
    public EncounterResponse findById(UUID id) {
        return encounterMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EncounterResponse> findByPatientId(UUID patientId) {
        return encounterRepository.findByPatientId(patientId).stream()
                .map(encounterMapper::toResponse)
                .toList();
    }

    public EncounterResponse finish(UUID id, Instant endedAt) {
        Encounter encounter = getOpenOrThrow(id);
        encounter.finish(endedAt);
        return encounterMapper.toResponse(encounterRepository.save(encounter));
    }

    public EncounterResponse cancel(UUID id, String reason) {
        Encounter encounter = getOpenOrThrow(id);
        encounter.cancel(reason);
        return encounterMapper.toResponse(encounterRepository.save(encounter));
    }

    //HELPERS
    private Encounter getOrThrow(UUID id) {
        return encounterRepository.findById(id).orElseThrow(()-> new EncounterNotFoundException(id));
    }

    private Encounter getOpenOrThrow(UUID id) {
        Encounter encounter = getOrThrow(id);
        if (!encounter.isOpen()) {
            throw new EncounterClosedException(id);
        }
        return encounter;
    }

}
