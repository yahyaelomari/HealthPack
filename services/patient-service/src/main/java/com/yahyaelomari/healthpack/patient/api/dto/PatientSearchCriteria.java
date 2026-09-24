package com.yahyaelomari.healthpack.patient.api.dto;

import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.PatientStatus;

import java.time.LocalDate;

/**
 * Search filters, bound from query parameters. Every field is optional and null
 * means "don't filter on this".
 *
 * <p>Feed this to a Spring Data {@code Specification} rather than writing one
 * repository method per combination — otherwise you end up with
 * {@code findByLastNameAndGenderAndStatusAndBirthDate...}.
 */
public record PatientSearchCriteria(

        String mrn,
        String lastName,
        String firstName,
        LocalDate birthDate,
        Gender gender,
        PatientStatus status
) {

    public boolean isEmpty() {
        return mrn == null
                && lastName == null
                && firstName == null
                && birthDate == null
                && gender == null
                && status == null;
    }
}
