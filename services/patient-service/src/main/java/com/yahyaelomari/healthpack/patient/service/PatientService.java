package com.yahyaelomari.healthpack.patient.service;

import com.yahyaelomari.healthpack.patient.api.dto.AddressDto;
import com.yahyaelomari.healthpack.patient.api.dto.ContactInfoDto;
import com.yahyaelomari.healthpack.patient.api.dto.PatientResponse;
import com.yahyaelomari.healthpack.patient.api.dto.PatientSearchCriteria;
import com.yahyaelomari.healthpack.patient.api.dto.PatientSummaryResponse;
import com.yahyaelomari.healthpack.patient.api.dto.RegisterPatientRequest;
import com.yahyaelomari.healthpack.patient.api.dto.UpdatePatientRequest;
import com.yahyaelomari.healthpack.patient.domain.Address;
import com.yahyaelomari.healthpack.patient.domain.ContactInfo;
import com.yahyaelomari.healthpack.patient.domain.Patient;
import com.yahyaelomari.healthpack.patient.exception.PatientNotFoundException;
import com.yahyaelomari.healthpack.patient.exception.PatientVersionConflictException;
import com.yahyaelomari.healthpack.patient.mapper.PatientMapper;
import com.yahyaelomari.healthpack.patient.repository.PatientRepository;
import com.yahyaelomari.healthpack.patient.repository.PatientSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Everything a caller can do with a patient, in plain steps: look one up,
 * build one, change one, or search for several.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final MrnGenerator mrnGenerator;

    public PatientResponse register(RegisterPatientRequest request) {
        Patient patient = Patient.builder()
                .mrn(mrnGenerator.generate())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .contact(toContactInfo(request.contact()))
                .build();

        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(UUID id) {
        return patientMapper.toResponse(getOrThrow(id));
    }

    public PatientResponse update(UUID id, UpdatePatientRequest request, long expectedVersion) {
        Patient patient = getOrThrow(id);

        if (patient.getVersion() != expectedVersion) {
            throw new PatientVersionConflictException();
        }

        patient.updateDemographics(request.firstName(), request.lastName(), request.birthDate(), request.gender());
        patient.updateContact(toContactInfo(request.contact()));

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public Page<PatientSummaryResponse> search(PatientSearchCriteria criteria, Pageable pageable) {
        return patientRepository.findAll(PatientSpecifications.fromCriteria(criteria), pageable)
                .map(patientMapper::toSummary);
    }

    public PatientResponse recordDeath(UUID id, LocalDate dateOfDeath) {
        Patient patient = getOrThrow(id);
        patient.recordDeath(dateOfDeath);
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    public PatientResponse deactivate(UUID id) {
        Patient patient = getOrThrow(id);
        patient.deactivate();
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    public PatientResponse reactivate(UUID id) {
        Patient patient = getOrThrow(id);
        patient.reactivate();
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    private Patient getOrThrow(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    // The mapper only goes domain -> DTO (see PatientMapper's javadoc), so the
    // other direction — building domain objects out of a request — is written
    // by hand here, and shared by register() and update() since both need it.
    private ContactInfo toContactInfo(ContactInfoDto dto) {
        return ContactInfo.builder()
                .email(dto.email())
                .phone(dto.phone())
                .address(toAddress(dto.address()))
                .build();
    }

    private Address toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        return Address.builder()
                .line1(dto.line1())
                .line2(dto.line2())
                .city(dto.city())
                .postalCode(dto.postalCode())
                .countryCode(dto.countryCode())
                .build();
    }
}
