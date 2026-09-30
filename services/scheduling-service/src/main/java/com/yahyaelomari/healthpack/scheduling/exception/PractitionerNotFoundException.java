package com.yahyaelomari.healthpack.scheduling.exception;

import java.util.UUID;

public class PractitionerNotFoundException extends RuntimeException {

    public PractitionerNotFoundException(UUID id) {
        super("No practitioner found with id " + id);
    }
}
