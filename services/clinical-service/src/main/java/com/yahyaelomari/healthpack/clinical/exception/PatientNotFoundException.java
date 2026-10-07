package com.yahyaelomari.healthpack.clinical.exception;

import java.util.UUID;

// Guards a patientId handed to clinical-service by a caller. Distinct from
// the exceptions in patient-service and scheduling-service, which guard
// their own data.
public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("No patient found with id " + patientId);
    }
}