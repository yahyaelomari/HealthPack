package com.yahyaelomari.healthpack.clinical.domain;

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
 * A single visit: one patient, one practitioner, a start and an end.
 *
 * <p>{@code patientId}, {@code practitionerId} and {@code appointmentId} are
 * plain UUIDs rather than JPA relationships — every one of them points at a
 * row in a different service's database, where a real foreign key cannot
 * exist. Whether the patient is real is checked once over gRPC when the
 * encounter is opened (Feat #33), not enforced by this table.
 *
 * <p>{@code appointmentId} is nullable because not every encounter was
 * booked: walk-ins and emergencies have no appointment to point at.
 *
 * <p>{@code @Builder} sits on the constructor, not the class, so building an
 * encounter still runs the validation below instead of skipping it the way a
 * generated all-args constructor would.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@Entity
@Table(name = "encounter")
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "patient_id", nullable = false, updatable = false)
    private UUID patientId;

    @Column(name = "practitioner_id", nullable = false, updatable = false)
    private UUID practitionerId;

    @Column(name = "appointment_id", updatable = false)
    private UUID appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private EncounterType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private EncounterStatus status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

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
    public Encounter(UUID patientId,
                     UUID practitionerId,
                     UUID appointmentId,
                     EncounterType type,
                     Instant startedAt,
                     String reason) {
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.practitionerId = Objects.requireNonNull(practitionerId, "practitionerId is required");
        this.appointmentId = appointmentId;
        this.type = Objects.requireNonNull(type, "type is required");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt is required");
        this.reason = reason;
        this.status = EncounterStatus.IN_PROGRESS;
    }

    // ── behaviour ────────────────────────────────────────────────────────────

    public void finish(Instant endedAt) {
        if (status != EncounterStatus.IN_PROGRESS) {
            throw new IllegalStateException("only an in-progress encounter can be finished, was " + status);
        }
        this.endedAt = requireAfterStart(endedAt, this.startedAt);
        this.status = EncounterStatus.FINISHED;
    }

    public void cancel(String reason) {
        if (status != EncounterStatus.IN_PROGRESS) {
            throw new IllegalStateException("only an in-progress encounter can be cancelled, was " + status);
        }
        this.status = EncounterStatus.CANCELLED;
        this.reason = reason;
    }

    // ── derived values ───────────────────────────────────────────────────────

    public boolean isOpen() {
        return status == EncounterStatus.IN_PROGRESS;
    }

    // ── identity ─────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Encounter other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Encounter.class.hashCode();
    }

    @Override
    public String toString() {
        return "Encounter{id=" + id + ", patientId=" + patientId + ", status=" + status + "}";
    }

    private static Instant requireAfterStart(Instant endedAt, Instant startedAt) {
        Objects.requireNonNull(endedAt, "endedAt is required");
        if (!endedAt.isAfter(startedAt)) {
            throw new IllegalArgumentException("endedAt must be after startedAt");
        }
        return endedAt;
    }
}
