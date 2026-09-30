package com.yahyaelomari.healthpack.patient.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record RecordDeathRequest(

        @NotNull
        @PastOrPresent(message = "date of death cannot be in the future")
        LocalDate dateOfDeath
) {
}
