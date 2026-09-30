package com.yahyaelomari.healthpack.scheduling.api;

import com.yahyaelomari.healthpack.scheduling.api.dto.PractitionerResponse;
import com.yahyaelomari.healthpack.scheduling.api.dto.RegisterPractitionerRequest;
import com.yahyaelomari.healthpack.scheduling.service.PractitionerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practitioners")
@RequiredArgsConstructor
public class PractitionerController {

    private final PractitionerService practitionerService;

    @PostMapping
    public ResponseEntity<PractitionerResponse> register(@Valid @RequestBody RegisterPractitionerRequest request) {
        PractitionerResponse response = practitionerService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/practitioners/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public PractitionerResponse findById(@PathVariable UUID id) {
        return practitionerService.findById(id);
    }

    @GetMapping
    public Page<PractitionerResponse> findAll(Pageable pageable) {
        return practitionerService.findAll(pageable);
    }
}
