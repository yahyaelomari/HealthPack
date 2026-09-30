package com.yahyaelomari.healthpack.patient.api;

import com.yahyaelomari.healthpack.patient.api.dto.ContactInfoDto;
import com.yahyaelomari.healthpack.patient.api.dto.PatientResponse;
import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.PatientStatus;
import com.yahyaelomari.healthpack.patient.exception.PatientNotFoundException;
import com.yahyaelomari.healthpack.patient.exception.PatientVersionConflictException;
import com.yahyaelomari.healthpack.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code @WebMvcTest} only starts the web layer for {@link PatientController}
 * — no database, no full application context. {@link PatientService} is
 * mocked, so these tests check one thing: does the controller talk HTTP
 * correctly, including routing exceptions through {@link PatientExceptionHandler}.
 *
 * <p>Request bodies are plain JSON text, not built via {@code ObjectMapper}:
 * Boot 4's {@code spring-boot-starter-jackson} wires up {@code tools.jackson}
 * (Jackson 3) as the injectable bean, not the classic
 * {@code com.fasterxml.jackson.databind.ObjectMapper} — easy to reach for the
 * wrong one and get a "no such bean" error. Writing the JSON directly avoids
 * the question entirely.
 */
// Security isn't wired up yet — that's Feat #6, once Keycloak's realm exists
// in M2 — but Spring Security auto-locks every endpoint the moment its
// dependency is on the classpath, which breaks an unauthenticated MockMvc
// call. addFilters = false skips that filter chain for this test only.
@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @Test
    void registerReturns201WithALocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(patientService.register(any())).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/v1/patients")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "birthDate": "1990-01-01",
                                  "gender": "MALE",
                                  "contact": { "email": "john@example.com" }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/patients/" + id));
    }

    @Test
    void findByIdReturns404WhenThePatientDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(patientService.findById(id)).thenThrow(new PatientNotFoundException(id));

        mockMvc.perform(get("/api/v1/patients/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturns409WhenTheCallerHasAStaleVersion() throws Exception {
        UUID id = UUID.randomUUID();
        when(patientService.update(eq(id), any(), eq(1L)))
                .thenThrow(new PatientVersionConflictException());

        mockMvc.perform(put("/api/v1/patients/{id}", id)
                        .header("If-Match", "1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "birthDate": "1990-01-01",
                                  "gender": "FEMALE",
                                  "contact": { "email": "jane@example.com" }
                                }
                                """))
                .andExpect(status().isConflict());
    }

    private PatientResponse sampleResponse(UUID id) {
        return new PatientResponse(id, "P-2026-000001", "John", "Doe", "John Doe",
                LocalDate.of(1990, 1, 1), 36, Gender.MALE, PatientStatus.ACTIVE, null,
                new ContactInfoDto("john@example.com", null, null),
                Instant.now(), Instant.now(), 0L);
    }
}
