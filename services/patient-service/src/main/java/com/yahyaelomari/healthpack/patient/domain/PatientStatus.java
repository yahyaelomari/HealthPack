package com.yahyaelomari.healthpack.patient.domain;

/**
 * Lifecycle of a patient record.
 *
 * <p>Records are never deleted — a patient who leaves becomes {@link #INACTIVE},
 * and a duplicate becomes {@link #MERGED}. Deleting would break every encounter,
 * result and invoice that references the id, and would defeat the audit trail.
 */
public enum PatientStatus {

    /** Normal state: the record can be used for new clinical activity. */
    ACTIVE,

    /** Retained for history, but no new activity should be recorded against it. */
    INACTIVE,

    /** The patient has died. Set together with a deceased date. */
    DECEASED,

    /** Found to be a duplicate and superseded by another record. */
    MERGED;

    public boolean acceptsNewActivity() {
        return this == ACTIVE;
    }
}
