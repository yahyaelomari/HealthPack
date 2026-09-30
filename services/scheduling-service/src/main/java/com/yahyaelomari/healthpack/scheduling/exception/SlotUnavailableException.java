package com.yahyaelomari.healthpack.scheduling.exception;

/**
 * Thrown when a booking or reschedule loses to
 * {@code no_double_booking}, the exclusion constraint in
 * {@code V1__scheduling.sql}.
 *
 * <p>Losing that race surfaces two different ways from Postgres — a clean
 * constraint violation, or a genuine deadlock when two brand-new inserts
 * contend for the same GiST lock at once (see {@code AppointmentRepositoryTest}
 * from Feat #13). Both mean the same thing to a caller: this slot is not
 * available. {@link com.yahyaelomari.healthpack.scheduling.service.AppointmentService}
 * catches both and throws this one exception either way, so nothing above
 * the service layer needs to know Postgres has two ways of saying no.
 */
public class SlotUnavailableException extends RuntimeException {

    public SlotUnavailableException() {
        super("This slot is no longer available");
    }
}
