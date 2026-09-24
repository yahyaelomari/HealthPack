package com.yahyaelomari.healthpack.patient.api.dto;

import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.PatientStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A full patient record.
 *
 * <p>{@code fullName} and {@code age} are derived server-side so that every
 * client — web, mobile, the BFF — formats them the same way.
 */
public record PatientResponse(

        UUID id,
        String mrn,
        String firstName,
        String lastName,
        String fullName,
        LocalDate birthDate,
        int age,
        Gender gender,
        PatientStatus status,
        LocalDate deceasedDate,
        ContactInfoDto contact,
        Instant createdAt,
        Instant updatedAt,

        /** Echo this back in {@code If-Match} on the next update. */
        long version
) {
}
