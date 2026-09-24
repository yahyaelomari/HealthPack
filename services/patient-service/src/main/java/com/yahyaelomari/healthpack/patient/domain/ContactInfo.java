package com.yahyaelomari.healthpack.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * How to reach a patient. Value object, embedded into the patient row.
 *
 * <p>Email and phone are protected health information. In M11 these two columns
 * get a JPA {@code AttributeConverter} backed by a Vault key — which is why they
 * are isolated here rather than spread across the entity.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@AllArgsConstructor
@Builder
@Embeddable
public class ContactInfo {

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 32)
    private String phone;

    @Embedded
    private Address address;

    /** True when there is no way at all to contact this patient. */
    public boolean isEmpty() {
        return email == null && phone == null && address == null;
    }
}
