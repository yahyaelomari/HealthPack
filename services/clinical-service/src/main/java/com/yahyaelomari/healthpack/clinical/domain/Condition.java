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
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Something diagnosed during an encounter.
 *
 * <p>Mapped to {@code clinical_condition}, not {@code condition}: CONDITION is
 * a reserved word in the SQL standard, and dodging it is cheaper than finding
 * out which dialect cares.
 *
 * <p>{@code onsetDate} is a LocalDate, not an Instant — when a patient says
 * the pain started "sometime last Tuesday", pretending to know the second it
 * happened would be a lie the type system would then enforce forever.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@Entity
@Table(name = "clinical_condition")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "encounter_id", nullable = false, updatable = false)
    private UUID encounterId;

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "display", nullable = false, length = 200)
    private String display;

    @Enumerated(EnumType.STRING)
    @Column(name = "clinical_status", nullable = false, length = 16)
    private ClinicalStatus clinicalStatus;

    @Column(name = "onset_date")
    private LocalDate onsetDate;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

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
    public Condition(UUID encounterId,
                     String code,
                     String display,
                     LocalDate onsetDate,
                     Instant recordedAt) {
        this.encounterId = Objects.requireNonNull(encounterId, "encounterId is required");
        this.code = requireText(code, "code");
        this.display = requireText(display, "display");
        this.onsetDate = onsetDate;
        this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt is required");
        this.clinicalStatus = ClinicalStatus.ACTIVE;
    }

    // ── behaviour ────────────────────────────────────────────────────────────

    public void resolve() {
        this.clinicalStatus = ClinicalStatus.RESOLVED;
    }

    public void markInRemission() {
        if (clinicalStatus != ClinicalStatus.ACTIVE) {
            throw new IllegalStateException("only an active condition can go into remission, was " + clinicalStatus);
        }
        this.clinicalStatus = ClinicalStatus.REMISSION;
    }

    // ── identity ─────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Condition other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Condition.class.hashCode();
    }

    @Override
    public String toString() {
        return "Condition{id=" + id + ", encounterId=" + encounterId + ", code=" + code + "}";
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
