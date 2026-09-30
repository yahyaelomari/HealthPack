package com.yahyaelomari.healthpack.patient.api;

import com.yahyaelomari.healthpack.patient.api.dto.PatientResponse;
import com.yahyaelomari.healthpack.patient.api.dto.PatientSearchCriteria;
import com.yahyaelomari.healthpack.patient.api.dto.PatientSummaryResponse;
import com.yahyaelomari.healthpack.patient.api.dto.RecordDeathRequest;
import com.yahyaelomari.healthpack.patient.api.dto.RegisterPatientRequest;
import com.yahyaelomari.healthpack.patient.api.dto.UpdatePatientRequest;
import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.PatientStatus;
import com.yahyaelomari.healthpack.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody RegisterPatientRequest request) {
        PatientResponse response = patientService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/patients/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public PatientResponse findById(@PathVariable UUID id) {
        return patientService.findById(id);
    }

    // If-Match carries the version the caller last read, not a real ETag —
    // see UpdatePatientRequest's javadoc for why.
    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable UUID id,
                                   @RequestHeader("If-Match") long ifMatch,
                                   @Valid @RequestBody UpdatePatientRequest request) {
        return patientService.update(id, request, ifMatch);
    }

    @GetMapping
    public Page<PatientSummaryResponse> search(@RequestParam(required = false) String mrn,
                                                @RequestParam(required = false) String lastName,
                                                @RequestParam(required = false) String firstName,
                                                @RequestParam(required = false) LocalDate birthDate,
                                                @RequestParam(required = false) Gender gender,
                                                @RequestParam(required = false) PatientStatus status,
                                                Pageable pageable) {
        PatientSearchCriteria criteria = new PatientSearchCriteria(mrn, lastName, firstName, birthDate, gender, status);
        return patientService.search(criteria, pageable);
    }

    @PostMapping("/{id}/deactivate")
    public PatientResponse deactivate(@PathVariable UUID id) {
        return patientService.deactivate(id);
    }

    @PostMapping("/{id}/reactivate")
    public PatientResponse reactivate(@PathVariable UUID id) {
        return patientService.reactivate(id);
    }

    @PostMapping("/{id}/record-death")
    public PatientResponse recordDeath(@PathVariable UUID id, @Valid @RequestBody RecordDeathRequest request) {
        return patientService.recordDeath(id, request.dateOfDeath());
    }
}
