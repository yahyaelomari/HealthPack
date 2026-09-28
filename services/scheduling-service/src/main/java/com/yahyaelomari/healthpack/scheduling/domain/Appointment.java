package com.yahyaelomari.healthpack.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A booked slot with a practitioner.
 *
 * <p>{@code practitionerId} and {@code patientId} are plain UUIDs, not JPA
 * relationships — {@code Practitioner} is a separate aggregate even though
 * it lives in the same database, and {@code patientId} refers to a row in a
 * different service's database entirely, where a real foreign key simply
 * cannot exist. Whether that patient is real is checked once, over gRPC, at
 * booking time — not enforced by this table.
 *
 * <p>The property that actually matters — two appointments for the same
 * practitioner can never overlap — is not enforced here at all. It is
 * enforced by {@code no_double_booking}, the exclusion constraint in
 * {@code V1__scheduling.sql}. An application-level check here would race
 * under concurrency; the database constraint cannot.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "practitioner_id", nullable = false, updatable = false)
    private UUID practitionerId;

    @Column(name = "patient_id", nullable = false, updatable = false)
    private UUID patientId;

    @Column(name = "slot_start", nullable = false)
    private Instant slotStart;

    @Column(name = "slot_end", nullable = false)
    private Instant slotEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AppointmentStatus status;

    @Column(name = "reason", length = 500)
    private String reason;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    public Appointment(UUID practitionerId, UUID patientId, Instant slotStart, Instant slotEnd, String reason) {
        this.practitionerId = Objects.requireNonNull(practitionerId, "practitionerId is required");
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.slotStart = Objects.requireNonNull(slotStart, "slotStart is required");
        this.slotEnd = requireAfterStart(slotEnd, this.slotStart);
        this.reason = reason;
        this.status = AppointmentStatus.REQUESTED;
    }

    // ── behaviour ────────────────────────────────────────────────────────────

    public void confirm() {
        if (status != AppointmentStatus.REQUESTED) {
            throw new IllegalStateException("only a requested appointment can be confirmed, was " + status);
        }
        this.status = AppointmentStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("a " + status + " appointment cannot be cancelled");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.reason = reason;
    }

    public void complete() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("only a confirmed appointment can be completed, was " + status);
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    public void reschedule(Instant newStart, Instant newEnd) {
        if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("a " + status + " appointment cannot be rescheduled");
        }
        this.slotStart = Objects.requireNonNull(newStart, "slotStart is required");
        this.slotEnd = requireAfterStart(newEnd, this.slotStart);
    }

    // ── derived values ───────────────────────────────────────────────────────

    public boolean overlaps(Appointment other) {
        return this.practitionerId.equals(other.practitionerId)
                && this.slotStart.isBefore(other.slotEnd)
                && other.slotStart.isBefore(this.slotEnd);
    }

    // ── identity ─────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appointment other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Appointment.class.hashCode();
    }

    @Override
    public String toString() {
        return "Appointment{id=" + id + ", practitionerId=" + practitionerId + ", status=" + status + "}";
    }

    private static Instant requireAfterStart(Instant slotEnd, Instant slotStart) {
        Objects.requireNonNull(slotEnd, "slotEnd is required");
        if (!slotEnd.isAfter(slotStart)) {
            throw new IllegalArgumentException("slotEnd must be after slotStart");
        }
        return slotEnd;
    }
}
