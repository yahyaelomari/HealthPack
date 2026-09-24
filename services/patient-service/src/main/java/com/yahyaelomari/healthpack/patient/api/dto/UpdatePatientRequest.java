package com.yahyaelomari.healthpack.patient.api.dto;

import com.yahyaelomari.healthpack.patient.domain.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Replaces a patient's demographics and contact details.
 *
 * <p>Neither the MRN nor the status can be changed here: the MRN is immutable, and
 * status transitions are separate operations because each has its own rules.
 *
 * <p>Send the version you last read in an {@code If-Match} header so a concurrent
 * edit is rejected rather than silently overwritten.
 */
public record UpdatePatientRequest(

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
