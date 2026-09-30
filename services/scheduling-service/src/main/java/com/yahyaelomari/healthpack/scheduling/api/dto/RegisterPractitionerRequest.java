package com.yahyaelomari.healthpack.scheduling.api.dto;

import com.yahyaelomari.healthpack.scheduling.domain.Specialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterPractitionerRequest(

        @NotBlank
        @Size(max = 20)
        String npi,

        @NotBlank
        @Size(max = 200)
        String fullName,

        @NotNull
        Specialty specialty
) {
}
