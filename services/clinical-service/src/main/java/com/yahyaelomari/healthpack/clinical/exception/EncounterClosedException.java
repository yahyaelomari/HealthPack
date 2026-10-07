package com.yahyaelomari.healthpack.clinical.exception;

import java.util.UUID;

/**
 * Thrown when something tries to change or add to an encounter that is
 * already FINISHED or CANCELLED. Once a visit is closed nothing more can be
 * attached to it, which is what makes the record trustworthy.
 */
public class EncounterClosedException extends RuntimeException {

    public EncounterClosedException(UUID id) {
        super("Encounter " + id + " is no longer in progress");
    }
}