package com.yahyaelomari.healthpack.clinical.api.dto;

import com.yahyaelomari.healthpack.clinical.domain.ClinicalStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ConditionResponse(

        UUID id,
        UUID encounterId,
        String code,
        String display,
        ClinicalStatus clinicalStatus,
        LocalDate onsetDate,
        Instant recordedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}