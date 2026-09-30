package com.yahyaelomari.healthpack.scheduling.exception;

import java.util.UUID;

// Distinct from patient-service's own PatientNotFoundException: that one
// guards patient-service's own data, this one guards a patientId handed to
// scheduling-service by a caller.
public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("No patient found with id " + patientId);
    }
}
