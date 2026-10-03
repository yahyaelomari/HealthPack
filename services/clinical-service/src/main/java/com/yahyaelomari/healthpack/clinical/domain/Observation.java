package com.yahyaelomari.healthpack.clinical.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One thing measured during an encounter: a code, a number, a unit.
 *
 * <p>{@code encounterId} is a plain UUID even though the encounter lives in
 * this same database — the database still enforces it with a real foreign key
 * (see V1__clinical.sql), but Java keeps it as an id rather than a
 * {@code @ManyToOne}. Consistent with the rest of the codebase, and it means
 * nothing here can trip a lazy-loading exception with open-in-view disabled.
 *
 * <p>{@code value} is a BigDecimal, not a double. Clinical measurements get
 * compared against thresholds and printed on documents, and binary floating
 * point rounding has no place in either.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@Entity
@Table(name = "observation")
public class Observation {

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

    @Column(name = "value", nullable = false, precision = 12, scale = 3)
    private BigDecimal value;

    @Column(name = "unit", nullable = false, length = 32)
    private String unit;

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
    public Observation(UUID encounterId,
                       String code,
                       String display,
                       BigDecimal value,
                       String unit,
                       Instant recordedAt) {
        this.encounterId = Objects.requireNonNull(encounterId, "encounterId is required");
        this.code = requireText(code, "code");
        this.display = requireText(display, "display");
        this.value = Objects.requireNonNull(value, "value is required");
        this.unit = requireText(unit, "unit");
        this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt is required");
    }

    // ── identity ─────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Observation other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Observation.class.hashCode();
    }

    @Override
    public String toString() {
        return "Observation{id=" + id + ", encounterId=" + encounterId + ", code=" + code + "}";
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
