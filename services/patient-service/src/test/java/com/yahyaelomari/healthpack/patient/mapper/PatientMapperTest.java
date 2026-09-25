package com.yahyaelomari.healthpack.patient.mapper;

import com.yahyaelomari.healthpack.patient.api.dto.PatientResponse;
import com.yahyaelomari.healthpack.patient.domain.ContactInfo;
import com.yahyaelomari.healthpack.patient.domain.Gender;
import com.yahyaelomari.healthpack.patient.domain.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PatientMapperTest {

    // MapStruct writes the real implementation at compile time — this is that
    // generated class, not something written by hand.
    private final PatientMapper mapper = new PatientMapperImpl();

    @Test
    void mapsAPatientWithNoAddressWithoutBlowingUp() {
        Patient patient = Patient.builder()
                .mrn("P-2026-000001")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .contact(ContactInfo.builder().email("john@example.com").build())
                .build();

        PatientResponse response = mapper.toResponse(patient);

        assertThat(response.contact().email()).isEqualTo("john@example.com");
        assertThat(response.contact().address()).isNull();
        assertThat(response.fullName()).isEqualTo("John Doe");
    }
}
