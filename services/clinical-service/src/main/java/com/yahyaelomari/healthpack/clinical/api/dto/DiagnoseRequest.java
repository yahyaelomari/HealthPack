package com.yahyaelomari.healthpack.clinical.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

/**
 * {@code onsetDate} is optional and a plain date: a patient who says the pain
 * started "sometime last Tuesday" has not given a timestamp.
 */
public record DiagnoseRequest(

        @NotBlank
        @Size(max = 32)
        String code,

        @NotBlank
        @Size(max = 200)
        String display,

        @PastOrPresent
        LocalDate onsetDate,

        @NotNull
        @PastOrPresent
        Instant recordedAt
) {
}