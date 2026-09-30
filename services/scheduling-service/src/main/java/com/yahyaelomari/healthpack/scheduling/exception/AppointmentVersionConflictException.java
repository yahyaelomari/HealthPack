package com.yahyaelomari.healthpack.scheduling.exception;

/**
 * Thrown when a reschedule's {@code expectedVersion} doesn't match what is
 * actually stored — someone else changed this appointment in between the
 * caller reading it and writing back. Same reasoning as
 * {@code PatientVersionConflictException} in patient-service.
 */
public class AppointmentVersionConflictException extends RuntimeException {

    public AppointmentVersionConflictException() {
        super("This appointment was changed by someone else — reload and try again");
    }
}
