package com.yahyaelomari.healthpack.patient.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Contact details on the wire.
 *
 * <p>Every field is optional individually, but a patient with no contact route at
 * all cannot be sent a reminder or a result — enforce "at least one of these" in
 * the service layer, where you can return a proper validation error.
 */
public record ContactInfoDto(

        @Email
        @Size(max = 255)
        String email,

        @Pattern(regexp = "^\\+?[0-9][0-9 ()./-]{5,31}$", message = "must be a valid phone number")
        String phone,

        @Valid
        AddressDto address
) {
}
