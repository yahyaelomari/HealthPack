package com.yahyaelomari.healthpack.clinical.api;

import com.yahyaelomari.healthpack.clinical.api.dto.ObservationResponse;
import com.yahyaelomari.healthpack.clinical.api.dto.RecordObservationRequest;
import com.yahyaelomari.healthpack.clinical.service.ObservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Observations only exist inside an encounter, so the encounter id lives in
// the path. There is no GET-by-id, so the 201 carries no Location header.
@RestController
@RequestMapping("/api/v1/encounters/{encounterId}/observations")
@RequiredArgsConstructor
public class ObservationController {

    private final ObservationService observationService;

    @PostMapping
    public ResponseEntity<ObservationResponse> record(@PathVariable UUID encounterId,
                                                      @Valid @RequestBody RecordObservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(observationService.record(encounterId, request));
    }

    @GetMapping
    public List<ObservationResponse> findByEncounter(@PathVariable UUID encounterId) {
        return observationService.findByEncounterId(encounterId);
    }
}