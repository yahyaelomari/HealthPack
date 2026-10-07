package com.yahyaelomari.healthpack.clinical.service;

import com.yahyaelomari.healthpack.clinical.api.dto.ConditionResponse;
import com.yahyaelomari.healthpack.clinical.api.dto.DiagnoseRequest;
import com.yahyaelomari.healthpack.clinical.domain.Condition;
import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import com.yahyaelomari.healthpack.clinical.exception.ConditionNotFoundException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.mapper.ConditionMapper;
import com.yahyaelomari.healthpack.clinical.repository.ConditionRepository;
import com.yahyaelomari.healthpack.clinical.repository.EncounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConditionService {

    private final ConditionRepository conditionRepository;
    private final EncounterRepository encounterRepository;
    private final ConditionMapper conditionMapper;

    public ConditionResponse diagnose(UUID encounterId, DiagnoseRequest request) {
        requireOpenEncounter(encounterId);

        Condition condition = Condition.builder()
                .encounterId(encounterId)
                .code(request.code())
                .display(request.display())
                .onsetDate(request.onsetDate())
                .recordedAt(request.recordedAt())
                .build();

        return conditionMapper.toResponse(conditionRepository.save(condition));
    }

    @Transactional(readOnly = true)
    public List<ConditionResponse> findByEncounterId(UUID encounterId) {
        if (!encounterRepository.existsById(encounterId)) {
            throw new EncounterNotFoundException(encounterId);
        }
        return conditionRepository.findByEncounterId(encounterId).stream()
                .map(conditionMapper::toResponse)
                .toList();
    }

    public ConditionResponse resolve(UUID id) {
        Condition condition = conditionRepository.findById(id)
                .orElseThrow(() -> new ConditionNotFoundException(id));
        condition.resolve();
        return conditionMapper.toResponse(conditionRepository.save(condition));
    }

    private void requireOpenEncounter(UUID encounterId) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));
        if (!encounter.isOpen()) {
            throw new EncounterClosedException(encounterId);
        }
    }
}