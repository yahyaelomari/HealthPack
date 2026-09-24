package com.yahyaelomari.healthpack.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

/**
 * A patient.
 *
 * <p>Aggregate root: state changes go through the methods below, never through
 * setters, so an invariant can never be bypassed by a caller who forgot about it.
 * That is also why this class uses {@code @Getter} rather than {@code @Data} —
 * {@code @Data} would generate setters and defeat the point.
 *
 * <p>{@code @Builder} sits on the constructor, not the class, so building a
 * patient still runs every validation below instead of skipping it the way a
 * generated all-args constructor would.
 *
 * <p>Flyway owns the schema and Hibernate runs with {@code ddl-auto=validate},
 * so every column is named explicitly here. If the migration and these
 * annotations disagree, the service refuses to start — which is the point.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@Entity
@Table(
        name = "patient",
        uniqueConstraints = @UniqueConstraint(name = "uk_patient_mrn", columnNames = "mrn")
)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Medical record number: the identifier humans use. Assigned by the service
     * at registration and never changed, because it ends up printed on documents
     * and quoted over the phone.
     */
    @Column(name = "mrn", nullable = false, length = 32, updatable = false)
    private String mrn;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 16)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PatientStatus status;

    @Column(name = "deceased_date")
    private LocalDate deceasedDate;

    @Embedded
    private ContactInfo contact;

    /**
     * Optimistic lock. Two clinicians editing the same record concurrently means
     * the second write fails loudly instead of silently overwriting the first.
     */
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
    public Patient(String mrn,
                   String firstName,
                   String lastName,
                   LocalDate birthDate,
                   Gender gender,
                   ContactInfo contact) {
        this.mrn = requireText(mrn, "mrn");
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.birthDate = requireNotFuture(birthDate);
        this.gender = Objects.requireNonNull(gender, "gender is required");
        this.contact = Objects.requireNonNull(contact, "contact is required");
        this.status = PatientStatus.ACTIVE;
    }

    // ── behaviour ────────────────────────────────────────────────────────────

    public void updateDemographics(String firstName, String lastName, LocalDate birthDate, Gender gender) {
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.birthDate = requireNotFuture(birthDate);
        this.gender = Objects.requireNonNull(gender, "gender is required");
        if (deceasedDate != null && deceasedDate.isBefore(this.birthDate)) {
            throw new IllegalArgumentException("birth date cannot be after the recorded date of death");
        }
    }

    public void updateContact(ContactInfo contact) {
        this.contact = Objects.requireNonNull(contact, "contact is required");
    }

    public void recordDeath(LocalDate date) {
        Objects.requireNonNull(date, "date of death is required");
        if (date.isBefore(birthDate)) {
            throw new IllegalArgumentException("date of death cannot precede date of birth");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("date of death cannot be in the future");
        }
        this.deceasedDate = date;
        this.status = PatientStatus.DECEASED;
    }

    public void deactivate() {
        if (status == PatientStatus.DECEASED) {
            throw new IllegalStateException("a deceased patient cannot be deactivated");
        }
        this.status = PatientStatus.INACTIVE;
    }

    public void reactivate() {
        if (status != PatientStatus.INACTIVE) {
            throw new IllegalStateException("only an inactive patient can be reactivated");
        }
        this.status = PatientStatus.ACTIVE;
    }

    // ── derived values ───────────────────────────────────────────────────────

    /** Age in whole years — at death if the patient has died, otherwise today. */
    public int getAge() {
        LocalDate until = deceasedDate != null ? deceasedDate : LocalDate.now();
        return Period.between(birthDate, until).getYears();
    }

    public boolean isMinor() {
        return getAge() < 18;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isDeceased() {
        return status == PatientStatus.DECEASED;
    }

    // ── identity ─────────────────────────────────────────────────────────────
    //
    // Deliberately not @EqualsAndHashCode: for a JPA entity, "equal if every
    // field matches" is wrong (two draft patients with no id yet would be
    // "equal"), and a hash that changes as fields fill in silently breaks
    // HashSet membership. Identity equality on the id, once assigned, is the
    // safe rule, so it stays hand-written.

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Patient other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // Constant, not id-based: the id is null until the first flush, and a
        // hash that changes mid-lifecycle silently breaks HashSet membership.
        return Patient.class.hashCode();
    }

    @Override
    public String toString() {
        // No name, no contact details: this string ends up in logs.
        return "Patient{id=" + id + ", mrn=" + mrn + ", status=" + status + "}";
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.strip();
    }

    private static LocalDate requireNotFuture(LocalDate birthDate) {
        Objects.requireNonNull(birthDate, "birthDate is required");
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("birth date cannot be in the future");
        }
        return birthDate;
    }
}
