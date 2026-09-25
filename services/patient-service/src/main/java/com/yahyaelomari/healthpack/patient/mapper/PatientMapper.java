package com.yahyaelomari.healthpack.patient.mapper;

import com.yahyaelomari.healthpack.patient.api.dto.AddressDto;
import com.yahyaelomari.healthpack.patient.api.dto.ContactInfoDto;
import com.yahyaelomari.healthpack.patient.api.dto.PatientResponse;
import com.yahyaelomari.healthpack.patient.api.dto.PatientSummaryResponse;
import com.yahyaelomari.healthpack.patient.domain.Address;
import com.yahyaelomari.healthpack.patient.domain.ContactInfo;
import com.yahyaelomari.healthpack.patient.domain.Patient;
import org.mapstruct.Mapper;

/**
 * Converts a {@link Patient} into the DTOs it is exposed as.
 *
 * <p>This only goes one direction — domain to DTO. Building a {@link Patient}
 * from a request is done by hand in {@code PatientService}, because
 * construction has to run through {@link Patient}'s constructor to keep its
 * validation, and a generated mapper would either bypass that or need to lean
 * on MapStruct's more advanced builder-detection to avoid it. Plain code that
 * calls {@code Patient.builder()} directly is easier to read than either.
 *
 * <p>{@code componentModel = "spring"} is what makes MapStruct generate a
 * real {@code @Component} — {@code PatientMapperImpl}, written at compile
 * time — so this interface can be constructor-injected like any other bean.
 */
@Mapper(componentModel = "spring")
public interface PatientMapper {

    PatientResponse toResponse(Patient patient);

    PatientSummaryResponse toSummary(Patient patient);

    ContactInfoDto toDto(ContactInfo contactInfo);

    AddressDto toDto(Address address);
}
