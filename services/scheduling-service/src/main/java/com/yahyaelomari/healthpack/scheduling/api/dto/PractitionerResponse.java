package com.yahyaelomari.healthpack.scheduling.api.dto;

import com.yahyaelomari.healthpack.scheduling.domain.Specialty;

import java.time.Instant;
import java.util.UUID;

public record PractitionerResponse(

        UUID id,
        String npi,
        String fullName,
        Specialty specialty,
        Instant createdAt,
        Instant updatedAt,

        /** Echo this back in {@code If-Match} on the next update. */
        long version
) {
}
