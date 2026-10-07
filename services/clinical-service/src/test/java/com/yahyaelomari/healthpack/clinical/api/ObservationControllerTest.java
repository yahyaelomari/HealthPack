package com.yahyaelomari.healthpack.clinical.api;

import com.yahyaelomari.healthpack.clinical.api.dto.ObservationResponse;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.service.ObservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ObservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ObservationControllerTest {

    private static final String BODY = """
            {
              "code": "8867-4",
              "display": "Heart rate",
              "value": 72,
              "unit": "beats/min",
              "recordedAt": "2026-01-01T10:05:00Z"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObservationService observationService;

    @Test
    void recordReturns201() throws Exception {
        UUID encounterId = UUID.randomUUID();
        when(observationService.record(eq(encounterId), any())).thenReturn(sampleResponse(encounterId));

        mockMvc.perform(post("/api/v1/encounters/{encounterId}/observations", encounterId)
                        .contentType("application/json")
                        .content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void recordReturns404WhenTheEncounterDoesNotExist() throws Exception {
        UUID encounterId = UUID.randomUUID();
        when(observationService.record(eq(encounterId), any()))
                .thenThrow(new EncounterNotFoundException(encounterId));

        mockMvc.perform(post("/api/v1/encounters/{encounterId}/observations", encounterId)
                        .contentType("application/json")
                        .content(BODY))
                .andExpect(status().isNotFound());
    }

    // The rule that makes the record trustworthy: a closed encounter
    // reaches the client as a 409, not a raw 500.
    @Test
    void recordReturns409WhenTheEncounterIsClosed() throws Exception {
        UUID encounterId = UUID.randomUUID();
        when(observationService.record(eq(encounterId), any()))
                .thenThrow(new EncounterClosedException(encounterId));

        mockMvc.perform(post("/api/v1/encounters/{encounterId}/observations", encounterId)
                        .contentType("application/json")
                        .content(BODY))
                .andExpect(status().isConflict());
    }

    private ObservationResponse sampleResponse(UUID encounterId) {
        return new ObservationResponse(UUID.randomUUID(), encounterId, "8867-4", "Heart rate",
                new BigDecimal("72.000"), "beats/min", Instant.parse("2026-01-01T10:05:00Z"),
                Instant.now(), Instant.now(), 0L);
    }
}