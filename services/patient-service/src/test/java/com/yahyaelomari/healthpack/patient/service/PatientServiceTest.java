package com.yahyaelomari.healthpack.patient.service;

import com.yahyaelomari.healthpack.patient.api.dto.ContactInfoDto;
import com.yahyaelomari.healthpack.patient.api.dto.RegisterPatientRequest;
import com.yahyaelomari.healthpack.patient.api.dto.UpdatePatientRequest;
import com.yahyaelomari.healthpack.patient.domain.ContactInfo;
import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.Patient;
import com.yahyaelomari.healthpack.patient.exception.PatientNotFoundException;
import com.yahyaelomari.healthpack.patient.exception.PatientVersionConflictException;
import com.yahyaelomari.healthpack.patient.mapper.PatientMapper;
import com.yahyaelomari.healthpack.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Plain Mockito, no Spring context and no database — every collaborator is a
 * mock, so these tests only check {@link PatientService}'s own logic
 * (does it call the right thing, does it throw when it should).
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private MrnGenerator mrnGenerator;

    @InjectMocks
    private PatientService patientService;

    @Test
    void registerGeneratesAnMrnAndSavesThePatient() {
        when(mrnGenerator.generate()).thenReturn("P-2026-000001");
        when(patientRepository.save(any(Patient.class))).thenAnswer(call -> call.getArgument(0));

        patientService.register(newRegisterRequest());

        verify(patientRepository).save(argThat(patient -> patient.getMrn().equals("P-2026-000001")));
    }

    @Test
    void findByIdThrowsWhenThePatientDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(id))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void updateThrowsWhenTheCallerHasAStaleVersion() {
        UUID id = UUID.randomUUID();
        Patient existing = existingPatient();
        when(patientRepository.findById(id)).thenReturn(Optional.of(existing));

        long staleVersion = existing.getVersion() + 1;

        assertThatThrownBy(() -> patientService.update(id, newUpdateRequest(), staleVersion))
                .isInstanceOf(PatientVersionConflictException.class);
    }

    private RegisterPatientRequest newRegisterRequest() {
        return new RegisterPatientRequest(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                new ContactInfoDto("john@example.com", null, null)
        );
    }

    private UpdatePatientRequest newUpdateRequest() {
        return new UpdatePatientRequest(
                "Jane",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.FEMALE,
                new ContactInfoDto("jane@example.com", null, null)
        );
    }

    private Patient existingPatient() {
        return Patient.builder()
                .mrn("P-2026-000001")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .contact(ContactInfo.builder().email("john@example.com").build())
                .build();
    }
}
