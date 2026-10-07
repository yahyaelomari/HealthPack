package com.yahyaelomari.healthpack.clinical.api;

import com.yahyaelomari.healthpack.clinical.api.dto.EncounterResponse;
import com.yahyaelomari.healthpack.clinical.domain.EncounterStatus;
import com.yahyaelomari.healthpack.clinical.domain.EncounterType;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.service.EncounterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EncounterController.class)
@AutoConfigureMockMvc(addFilters = false)
class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EncounterService encounterService;

    @Test
    void openReturns201WithALocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(encounterService.open(any())).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/v1/encounters")
                        .contentType("application/json")
                        .content("""
                                {
                                  "patientId": "%s",
                                  "practitionerId": "%s",
                                  "type": "AMBULATORY",
                                  "startedAt": "2026-01-01T10:00:00Z",
                                  "reason": "check-up"
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/encounters/" + id));
    }

    @Test
    void openReturns400WhenThePatientIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/encounters")
                        .contentType("application/json")
                        .content("""
                                {
                                  "practitionerId": "%s",
                                  "type": "AMBULATORY",
                                  "startedAt": "2026-01-01T10:00:00Z"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finishReturns409WhenTheEncounterIsAlreadyClosed() throws Exception {
        UUID id = UUID.randomUUID();
        when(encounterService.finish(eq(id), any())).thenThrow(new EncounterClosedException(id));

        mockMvc.perform(post("/api/v1/encounters/{id}/finish", id)
                        .contentType("application/json")
                        .content("""
                                { "endedAt": "2026-01-01T10:30:00Z" }
                                """))
                .andExpect(status().isConflict());
    }

    private EncounterResponse sampleResponse(UUID id) {
        return new EncounterResponse(id, UUID.randomUUID(), UUID.randomUUID(), null,
                EncounterType.AMBULATORY, EncounterStatus.IN_PROGRESS,
                Instant.parse("2026-01-01T10:00:00Z"), null, "check-up",
                Instant.now(), Instant.now(), 0L);
    }
}