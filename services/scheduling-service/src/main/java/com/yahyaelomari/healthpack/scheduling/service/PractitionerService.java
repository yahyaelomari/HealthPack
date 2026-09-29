package com.yahyaelomari.healthpack.scheduling.service;

import com.yahyaelomari.healthpack.scheduling.api.dto.PractitionerResponse;
import com.yahyaelomari.healthpack.scheduling.api.dto.RegisterPractitionerRequest;
import com.yahyaelomari.healthpack.scheduling.domain.Practitioner;
import com.yahyaelomari.healthpack.scheduling.exception.PractitionerNotFoundException;
import com.yahyaelomari.healthpack.scheduling.mapper.PractitionerMapper;
import com.yahyaelomari.healthpack.scheduling.repository.PractitionerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PractitionerService {

    private final PractitionerRepository practitionerRepository;
    private final PractitionerMapper practitionerMapper;

    public PractitionerResponse register(RegisterPractitionerRequest request) {
        Practitioner practitioner = Practitioner.builder()
                .npi(request.npi())
                .fullName(request.fullName())
                .specialty(request.specialty())
                .build();

        return practitionerMapper.toResponse(practitionerRepository.save(practitioner));
    }

    @Transactional(readOnly = true)
    public PractitionerResponse findById(UUID id) {
        return practitionerMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PractitionerResponse> findAll(Pageable pageable) {
        return practitionerRepository.findAll(pageable).map(practitionerMapper::toResponse);
    }

    private Practitioner getOrThrow(UUID id) {
        return practitionerRepository.findById(id)
                .orElseThrow(() -> new PractitionerNotFoundException(id));
    }
}
