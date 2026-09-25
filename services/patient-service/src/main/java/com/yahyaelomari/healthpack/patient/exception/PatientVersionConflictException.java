package com.yahyaelomari.healthpack.patient.exception;

/**
 * Thrown when an update's {@code expectedVersion} doesn't match what is
 * actually stored — someone else changed this patient in between the caller
 * reading it and writing back.
 */
public class PatientVersionConflictException extends RuntimeException {

    public PatientVersionConflictException() {
        super("This patient record was changed by someone else — reload and try again");
    }
}
