package com.yahyaelomari.healthpack.clinical.api.dto;

import com.yahyaelomari.healthpack.clinical.domain.EncounterStatus;
import com.yahyaelomari.healthpack.clinical.domain.EncounterType;

import java.time.Instant;
import java.util.UUID;

public record EncounterResponse(

        UUID id,
        UUID patientId,
        UUID practitionerId,
        UUID appointmentId,
        EncounterType type,
        EncounterStatus status,
        Instant startedAt,
        Instant endedAt,
        String reason,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}