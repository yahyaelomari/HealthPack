package com.yahyaelomari.healthpack.scheduling.api;

import com.yahyaelomari.healthpack.scheduling.api.dto.AppointmentResponse;
import com.yahyaelomari.healthpack.scheduling.domain.AppointmentStatus;
import com.yahyaelomari.healthpack.scheduling.exception.AppointmentVersionConflictException;
import com.yahyaelomari.healthpack.scheduling.exception.SlotUnavailableException;
import com.yahyaelomari.healthpack.scheduling.service.AppointmentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void bookReturns201WithALocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.book(any())).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "practitionerId": "%s",
                                  "patientId": "%s",
                                  "slotStart": "2027-01-01T10:00:00Z",
                                  "slotEnd": "2027-01-01T10:30:00Z",
                                  "reason": "check-up"
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/appointments/" + id));
    }

    // The point of this test: SlotUnavailableException — which absorbs both
    // of no_double_booking's failure modes from Feat #13 — actually reaches
    // the client as a 409, not a raw 500.
    @Test
    void bookReturns409WhenTheSlotIsTaken() throws Exception {
        when(appointmentService.book(any())).thenThrow(new SlotUnavailableException());

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "practitionerId": "%s",
                                  "patientId": "%s",
                                  "slotStart": "2027-01-01T10:00:00Z",
                                  "slotEnd": "2027-01-01T10:30:00Z"
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isConflict());
    }

    @Test
    void rescheduleReturns409WhenTheCallerHasAStaleVersion() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.reschedule(eq(id), any(), eq(1L)))
                .thenThrow(new AppointmentVersionConflictException());

        mockMvc.perform(put("/api/v1/appointments/{id}/reschedule", id)
                        .header("If-Match", "1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "slotStart": "2027-01-02T10:00:00Z",
                                  "slotEnd": "2027-01-02T10:30:00Z"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    private AppointmentResponse sampleResponse(UUID id) {
        return new AppointmentResponse(id, UUID.randomUUID(), UUID.randomUUID(),
                Instant.parse("2027-01-01T10:00:00Z"), Instant.parse("2027-01-01T10:30:00Z"),
                AppointmentStatus.REQUESTED, "check-up", Instant.now(), Instant.now(), 0L);
    }
}
