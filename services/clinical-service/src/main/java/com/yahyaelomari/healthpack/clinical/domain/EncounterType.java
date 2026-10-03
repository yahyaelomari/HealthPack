package com.yahyaelomari.healthpack.clinical.domain;

/**
 * Where the encounter physically happened. Mirrors the subset of FHIR R4's
 * encounter class codes that this project actually uses.
 */
public enum EncounterType {

    AMBULATORY,
    EMERGENCY,
    INPATIENT,
    VIRTUAL
}
