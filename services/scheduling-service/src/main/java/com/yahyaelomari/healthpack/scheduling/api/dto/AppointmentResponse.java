package com.yahyaelomari.healthpack.scheduling.api.dto;

import com.yahyaelomari.healthpack.scheduling.domain.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(

        UUID id,
        UUID practitionerId,
        UUID patientId,
        Instant slotStart,
        Instant slotEnd,
        AppointmentStatus status,
        String reason,
        Instant createdAt,
        Instant updatedAt,

        /** Echo this back in {@code If-Match} when rescheduling. */
        long version
) {
}
