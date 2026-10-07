package com.yahyaelomari.healthpack.clinical.api.dto;

import com.yahyaelomari.healthpack.clinical.domain.EncounterType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code appointmentId} is optional: walk-ins and emergencies have no
 * appointment to point at.
 */
public record OpenEncounterRequest(

        @NotNull
        UUID patientId,

        @NotNull
        UUID practitionerId,

        UUID appointmentId,

        @NotNull
        EncounterType type,

        @NotNull
        @PastOrPresent(message = "an encounter cannot start in the future")
        Instant startedAt,

        @Size(max = 500)
        String reason
) {
}