package com.yahyaelomari.healthpack.patient.api.dto;

import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.PatientStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The slim shape used for search results and, from M3, for the gRPC
 * {@code BatchGetPatients} response.
 *
 * <p>Contact details are deliberately absent: a search result list should not
 * spray email addresses and phone numbers across the network just because
 * someone typed three letters of a surname.
 */
public record PatientSummaryResponse(

        UUID id,
        String mrn,
        String fullName,
        LocalDate birthDate,
        Gender gender,
        PatientStatus status
) {
}
