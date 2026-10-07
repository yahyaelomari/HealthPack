package com.yahyaelomari.healthpack.clinical.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * {@code @Digits} mirrors the {@code numeric(12,3)} column in
 * V1__clinical.sql, so a value that would not fit is rejected as a 400 here
 * instead of surfacing as a database error.
 */
public record RecordObservationRequest(

        @NotBlank
        @Size(max = 32)
        String code,

        @NotBlank
        @Size(max = 200)
        String display,

        @NotNull
        @Digits(integer = 9, fraction = 3)
        BigDecimal value,

        @NotBlank
        @Size(max = 32)
        String unit,

        @NotNull
        @PastOrPresent
        Instant recordedAt
) {
}