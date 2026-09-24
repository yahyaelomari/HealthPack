package com.yahyaelomari.healthpack.patient.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Postal address on the wire. All fields optional — plenty of patients have no fixed address. */
public record AddressDto(

        @Size(max = 200)
        String line1,

        @Size(max = 200)
        String line2,

        @Size(max = 100)
        String city,

        @Size(max = 20)
        String postalCode,

        @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a two-letter ISO 3166-1 country code")
        String countryCode
) {
}
