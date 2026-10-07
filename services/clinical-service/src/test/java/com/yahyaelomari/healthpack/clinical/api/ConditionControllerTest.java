package com.yahyaelomari.healthpack.clinical.api;

import com.yahyaelomari.healthpack.clinical.exception.ConditionNotFoundException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.service.ConditionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConditionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConditionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConditionService conditionService;

    @Test
    void diagnoseReturns409WhenTheEncounterIsClosed() throws Exception {
        UUID encounterId = UUID.randomUUID();
        when(conditionService.diagnose(eq(encounterId), any()))
                .thenThrow(new EncounterClosedException(encounterId));

        mockMvc.perform(post("/api/v1/encounters/{encounterId}/conditions", encounterId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "I10",
                                  "display": "Essential hypertension",
                                  "recordedAt": "2026-01-01T10:05:00Z"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void resolveReturns404WhenTheConditionDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(conditionService.resolve(id)).thenThrow(new ConditionNotFoundException(id));

        mockMvc.perform(post("/api/v1/conditions/{id}/resolve", id))
                .andExpect(status().isNotFound());
    }
}
