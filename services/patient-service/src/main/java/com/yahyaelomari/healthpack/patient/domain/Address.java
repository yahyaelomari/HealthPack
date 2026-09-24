package com.yahyaelomari.healthpack.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A postal address. Value object: it has no identity of its own, and two
 * addresses with the same fields are the same address.
 *
 * <p>{@code @Builder} sits on the constructor rather than the class, so
 * building still runs the country-code normalisation below instead of
 * bypassing it the way a generated all-args constructor would.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
@Embeddable
public class Address {

    @Column(name = "address_line1", length = 200)
    private String line1;

    @Column(name = "address_line2", length = 200)
    private String line2;

    @Column(name = "address_city", length = 100)
    private String city;

    @Column(name = "address_postal_code", length = 20)
    private String postalCode;

    /** ISO 3166-1 alpha-2, uppercase. */
    @Column(name = "address_country_code", length = 2)
    private String countryCode;

    @Builder
    public Address(String line1, String line2, String city, String postalCode, String countryCode) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.postalCode = postalCode;
        this.countryCode = countryCode == null ? null : countryCode.toUpperCase();
    }
}
