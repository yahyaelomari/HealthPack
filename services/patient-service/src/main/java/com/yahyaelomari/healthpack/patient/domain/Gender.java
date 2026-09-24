package com.yahyaelomari.healthpack.patient.domain;

/**
 * Administrative gender, as recorded for identification and correspondence.
 *
 * <p>The four values mirror FHIR R4 {@code AdministrativeGender} so the FHIR
 * facade can map them without a lookup table. This is deliberately not a
 * clinical or biological statement — observations carry that, not the patient
 * record.
 */
public enum Gender {

    MALE("male"),
    FEMALE("female"),
    OTHER("other"),
    UNKNOWN("unknown");

    private final String fhirCode;

    Gender(String fhirCode) {
        this.fhirCode = fhirCode;
    }

    /** The lowercase token FHIR expects on the wire. */
    public String fhirCode() {
        return fhirCode;
    }
}
