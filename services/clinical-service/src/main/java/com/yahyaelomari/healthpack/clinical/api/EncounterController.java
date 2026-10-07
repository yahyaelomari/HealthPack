package com.yahyaelomari.healthpack.clinical.api;

import com.yahyaelomari.healthpack.clinical.api.dto.CancelEncounterRequest;
import com.yahyaelomari.healthpack.clinical.api.dto.EncounterResponse;
import com.yahyaelomari.healthpack.clinical.api.dto.FinishEncounterRequest;
import com.yahyaelomari.healthpack.clinical.api.dto.OpenEncounterRequest;
import com.yahyaelomari.healthpack.clinical.service.EncounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/encounters")
@RequiredArgsConstructor
public class EncounterController {

    private final EncounterService encounterService;

    @PostMapping
    public ResponseEntity<EncounterResponse> open(@Valid @RequestBody OpenEncounterRequest request) {
        EncounterResponse response = encounterService.open(request);
        return ResponseEntity.created(URI.create("/api/v1/encounters/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public EncounterResponse findById(@PathVariable UUID id) {
        return encounterService.findById(id);
    }

    @GetMapping
    public List<EncounterResponse> findByPatient(@RequestParam UUID patientId) {
        return encounterService.findByPatientId(patientId);
    }

    @PostMapping("/{id}/finish")
    public EncounterResponse finish(@PathVariable UUID id, @Valid @RequestBody FinishEncounterRequest request) {
        return encounterService.finish(id, request.endedAt());
    }

    @PostMapping("/{id}/cancel")
    public EncounterResponse cancel(@PathVariable UUID id, @Valid @RequestBody CancelEncounterRequest request) {
        return encounterService.cancel(id, request.reason());
    }
}