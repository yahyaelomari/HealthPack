package com.yahyaelomari.healthpack.clinical.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FinishEncounterRequest(

        @NotNull
        Instant endedAt
) {
}