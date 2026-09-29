package com.yahyaelomari.healthpack.scheduling.exception;

import java.util.UUID;

public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(UUID id) {
        super("No appointment found with id " + id);
    }
}
