package com.yahyaelomari.healthpack.patient.grpc;

import com.yahyaelomari.healthpack.patient.domain.ContactInfo;
import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.Patient;
import com.yahyaelomari.healthpack.patient.repository.PatientRepository;
import com.yahyaelomari.healthpack.proto.patient.GetPatientRequest;
import com.yahyaelomari.healthpack.proto.patient.GetPatientResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientGrpcServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private StreamObserver<GetPatientResponse> responseObserver;

    @InjectMocks
    private PatientGrpcService patientGrpcService;

    @Test
    void returnsFoundWithThePatientWhenItExists() {
        UUID id = UUID.randomUUID();
        Patient patient = Patient.builder()
                .mrn("P-2026-000001")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .contact(ContactInfo.builder().email("john@example.com").build())
                .build();
        ReflectionTestUtils.setField(patient, "id", id);
        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));

        patientGrpcService.getPatient(
                GetPatientRequest.newBuilder().setId(id.toString()).build(), responseObserver);

        ArgumentCaptor<GetPatientResponse> captor = ArgumentCaptor.forClass(GetPatientResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        GetPatientResponse response = captor.getValue();
        assertThat(response.getFound()).isTrue();
        assertThat(response.getId()).isEqualTo(id.toString());
        assertThat(response.getMrn()).isEqualTo("P-2026-000001");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
    }

    @Test
    void returnsNotFoundWhenThePatientDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        patientGrpcService.getPatient(
                GetPatientRequest.newBuilder().setId(id.toString()).build(), responseObserver);

        ArgumentCaptor<GetPatientResponse> captor = ArgumentCaptor.forClass(GetPatientResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        assertThat(captor.getValue().getFound()).isFalse();
    }
}
