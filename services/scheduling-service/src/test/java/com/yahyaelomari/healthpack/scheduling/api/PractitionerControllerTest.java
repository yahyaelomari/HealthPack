package com.yahyaelomari.healthpack.scheduling.api;

import com.yahyaelomari.healthpack.scheduling.api.dto.PractitionerResponse;
import com.yahyaelomari.healthpack.scheduling.domain.Specialty;
import com.yahyaelomari.healthpack.scheduling.exception.PractitionerNotFoundException;
import com.yahyaelomari.healthpack.scheduling.service.PractitionerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Security isn't wired up for scheduling-service yet — same reasoning as
// patient-service's PatientControllerTest: the dependency alone makes Spring
// Security lock every request down by default the moment the app starts.
@WebMvcTest(PractitionerController.class)
@AutoConfigureMockMvc(addFilters = false)
class PractitionerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PractitionerService practitionerService;

    @Test
    void registerReturns201WithALocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(practitionerService.register(any())).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/v1/practitioners")
                        .contentType("application/json")
                        .content("""
                                {
                                  "npi": "1234567890",
                                  "fullName": "Dr. Jamie Doe",
                                  "specialty": "CARDIOLOGY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/practitioners/" + id));
    }

    @Test
    void findByIdReturns404WhenThePractitionerDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(practitionerService.findById(id)).thenThrow(new PractitionerNotFoundException(id));

        mockMvc.perform(get("/api/v1/practitioners/{id}", id))
                .andExpect(status().isNotFound());
    }

    private PractitionerResponse sampleResponse(UUID id) {
        return new PractitionerResponse(id, "1234567890", "Dr. Jamie Doe", Specialty.CARDIOLOGY,
                Instant.now(), Instant.now(), 0L);
    }
}
