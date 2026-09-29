package com.yahyaelomari.healthpack.scheduling.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RescheduleAppointmentRequest(

        @NotNull
        @Future(message = "an appointment cannot be rescheduled into the past")
        Instant slotStart,

        @NotNull
        Instant slotEnd
) {
}
