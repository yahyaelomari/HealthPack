package com.yahyaelomari.healthpack.clinical.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelEncounterRequest(

        @NotBlank
        @Size(max = 500)
        String reason
) {
}
