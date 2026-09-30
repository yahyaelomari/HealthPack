package com.yahyaelomari.healthpack.scheduling.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Books a specific time directly — there is no {@code AvailabilityRule} or
 * pre-generated slot list here (a deliberate scope decision made in Feat
 * #13). The client proposes a time; the database's exclusion constraint is
 * what actually decides whether it is free.
 */
public record BookAppointmentRequest(

        @NotNull
        UUID practitionerId,

        @NotNull
        UUID patientId,

        @NotNull
        @Future(message = "an appointment cannot be booked in the past")
        Instant slotStart,

        @NotNull
        Instant slotEnd,

        @Size(max = 500)
        String reason
) {
}
