package com.yahyaelomari.healthpack.scheduling.domain;

/**
 * Lifecycle of an appointment.
 *
 * <p>{@code CANCELLED} is deliberately excluded from the double-booking
 * check in {@code V1__scheduling.sql} — a cancelled appointment must free up
 * its slot, not keep blocking it forever.
 */
public enum AppointmentStatus {

    REQUESTED,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
