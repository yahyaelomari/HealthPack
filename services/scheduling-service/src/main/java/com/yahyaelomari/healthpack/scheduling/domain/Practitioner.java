package com.yahyaelomari.healthpack.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
 * A practitioner who can be booked. Owned by scheduling-service, not
 * identity-service or clinical-service — the gateway already routes
 * {@code /api/v1/practitioners/**} here.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@Entity
@Table(
        name = "practitioner",
        uniqueConstraints = @UniqueConstraint(name = "uk_practitioner_npi", columnNames = "npi")
)
public class Practitioner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "npi", nullable = false, length = 20, updatable = false)
    private String npi;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false, length = 50)
    private Specialty specialty;

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
    public Practitioner(String npi, String fullName, Specialty specialty) {
        this.npi = requireText(npi, "npi");
        this.fullName = requireText(fullName, "fullName");
        this.specialty = Objects.requireNonNull(specialty, "specialty is required");
    }

    public void rename(String fullName) {
        this.fullName = requireText(fullName, "fullName");
    }

    public void changeSpecialty(Specialty specialty) {
        this.specialty = Objects.requireNonNull(specialty, "specialty is required");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Practitioner other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Practitioner.class.hashCode();
    }

    @Override
    public String toString() {
        return "Practitioner{id=" + id + ", npi=" + npi + "}";
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.strip();
    }
}
