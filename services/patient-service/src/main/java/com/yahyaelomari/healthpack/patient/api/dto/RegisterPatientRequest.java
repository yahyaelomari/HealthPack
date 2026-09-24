package com.yahyaelomari.healthpack.patient.api.dto;

import com.yahyaelomari.healthpack.patient.domain.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Registers a new patient.
 *
 * <p>There is no MRN field on purpose: the service assigns it. Letting a client
 * choose the medical record number invites collisions and lets an attacker probe
 * for existing patients.
 */
public record RegisterPatientRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotNull
        @Past(message = "birth date must be in the past")
        LocalDate birthDate,

        @NotNull
        Gender gender,

        @NotNull
        @Valid
        ContactInfoDto contact
) {
}
