package com.yahyaelomari.healthpack.clinical.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ObservationResponse(

        UUID id,
        UUID encounterId,
        String code,
        String display,
        BigDecimal value,
        String unit,
        Instant recordedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}