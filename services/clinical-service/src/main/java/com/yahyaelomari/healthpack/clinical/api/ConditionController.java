package com.yahyaelomari.healthpack.clinical.api;

import com.yahyaelomari.healthpack.clinical.api.dto.ConditionResponse;
import com.yahyaelomari.healthpack.clinical.api.dto.DiagnoseRequest;
import com.yahyaelomari.healthpack.clinical.service.ConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Two different base paths on purpose: diagnosing and listing happen inside
// an encounter, but resolving acts on the condition itself, which outlives
// the visit that diagnosed it.
@RestController
@RequiredArgsConstructor
public class ConditionController {

    private final ConditionService conditionService;

    @PostMapping("/api/v1/encounters/{encounterId}/conditions")
    public ResponseEntity<ConditionResponse> diagnose(@PathVariable UUID encounterId,
                                                      @Valid @RequestBody DiagnoseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conditionService.diagnose(encounterId, request));
    }

    @GetMapping("/api/v1/encounters/{encounterId}/conditions")
    public List<ConditionResponse> findByEncounter(@PathVariable UUID encounterId) {
        return conditionService.findByEncounterId(encounterId);
    }

    @PostMapping("/api/v1/conditions/{id}/resolve")
    public ConditionResponse resolve(@PathVariable UUID id) {
        return conditionService.resolve(id);
    }
}