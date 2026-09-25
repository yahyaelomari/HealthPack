package com.yahyaelomari.healthpack.patient.exception;

import java.util.UUID;

/**
 * Thrown when a lookup by id finds nothing.
 *
 * <p>Turning this into an actual 404 response is Feat #6 — for now it's just
 * a clear, named failure the service layer can throw and a test can assert on.
 */
public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID id) {
        super("No patient found with id " + id);
    }
}
