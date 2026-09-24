package com.yahyaelomari.healthpack.patient.repository;

import com.yahyaelomari.healthpack.patient.api.dto.PatientSearchCriteria;
import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.Patient;
import com.yahyaelomari.healthpack.patient.domain.PatientStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link Specification} from {@link PatientSearchCriteria}.
 *
 * <p>Every field on the criteria is optional, and every method here treats a
 * null filter as "don't filter on this" rather than "match null". The
 * alternative — one repository method per combination of filters — stops being
 * maintainable at three optional fields; this one has five.
 */
public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    public static Specification<Patient> fromCriteria(PatientSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.mrn() != null) {
                predicates.add(cb.equal(root.get("mrn"), criteria.mrn()));
            }
            if (criteria.lastName() != null) {
                predicates.add(cb.like(cb.lower(root.get("lastName")),
                        "%" + criteria.lastName().toLowerCase() + "%"));
            }
            if (criteria.firstName() != null) {
                predicates.add(cb.like(cb.lower(root.get("firstName")),
                        "%" + criteria.firstName().toLowerCase() + "%"));
            }
            if (criteria.birthDate() != null) {
                predicates.add(cb.equal(root.get("birthDate"), criteria.birthDate()));
            }
            if (criteria.gender() != null) {
                predicates.add(cb.equal(root.get("gender"), criteria.gender()));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Kept as separate single-field specifications too: useful in isolation for
    // narrower queries, and easier to unit test one predicate at a time than the
    // combined criteria specification above.

    public static Specification<Patient> hasMrn(String mrn) {
        return (root, query, cb) -> cb.equal(root.get("mrn"), mrn);
    }

    public static Specification<Patient> lastNameContains(String lastName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("lastName")),
                "%" + lastName.toLowerCase() + "%");
    }

    public static Specification<Patient> firstNameContains(String firstName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("firstName")),
                "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<Patient> hasBirthDate(LocalDate birthDate) {
        return (root, query, cb) -> cb.equal(root.get("birthDate"), birthDate);
    }

    public static Specification<Patient> hasGender(Gender gender) {
        return (root, query, cb) -> cb.equal(root.get("gender"), gender);
    }

    public static Specification<Patient> hasStatus(PatientStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
