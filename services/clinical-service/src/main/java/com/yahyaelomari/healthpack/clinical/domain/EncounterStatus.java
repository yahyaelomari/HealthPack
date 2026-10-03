package com.yahyaelomari.healthpack.clinical.domain;

/**
 * Lifecycle of an encounter.
 *
 * <p>There is no PLANNED state on purpose: a planned visit is an appointment,
 * and that already lives in scheduling-service. An encounter only exists once
 * the visit is actually happening.
 */
public enum EncounterStatus {

    IN_PROGRESS,
    FINISHED,
    CANCELLED
}
