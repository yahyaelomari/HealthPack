package com.yahyaelomari.healthpack.scheduling.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(

        @NotBlank
        @Size(max = 500)
        String reason
) {
}
