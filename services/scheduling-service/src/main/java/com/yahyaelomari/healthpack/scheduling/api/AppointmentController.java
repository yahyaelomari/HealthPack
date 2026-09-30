package com.yahyaelomari.healthpack.scheduling.api;

import com.yahyaelomari.healthpack.scheduling.api.dto.AppointmentResponse;
import com.yahyaelomari.healthpack.scheduling.api.dto.BookAppointmentRequest;
import com.yahyaelomari.healthpack.scheduling.api.dto.CancelAppointmentRequest;
import com.yahyaelomari.healthpack.scheduling.api.dto.RescheduleAppointmentRequest;
import com.yahyaelomari.healthpack.scheduling.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody BookAppointmentRequest request) {
        AppointmentResponse response = appointmentService.book(request);
        return ResponseEntity.created(URI.create("/api/v1/appointments/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable UUID id) {
        return appointmentService.findById(id);
    }

    @GetMapping
    public List<AppointmentResponse> findByPatient(@RequestParam UUID patientId) {
        return appointmentService.findByPatientId(patientId);
    }

    @PostMapping("/{id}/confirm")
    public AppointmentResponse confirm(@PathVariable UUID id) {
        return appointmentService.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable UUID id, @Valid @RequestBody CancelAppointmentRequest request) {
        return appointmentService.cancel(id, request.reason());
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable UUID id) {
        return appointmentService.complete(id);
    }

    // If-Match carries the version the caller last read — same convention as
    // patient-service's PUT /patients/{id}.
    @PutMapping("/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable UUID id,
                                           @RequestHeader("If-Match") long ifMatch,
                                           @Valid @RequestBody RescheduleAppointmentRequest request) {
        return appointmentService.reschedule(id, request, ifMatch);
    }
}
