package com.yahyaelomari.healthpack.scheduling.mapper;

import com.yahyaelomari.healthpack.scheduling.api.dto.PractitionerResponse;
import com.yahyaelomari.healthpack.scheduling.domain.Practitioner;
import org.mapstruct.Mapper;

/**
 * Domain to DTO only — same reasoning as {@code PatientMapper} in
 * patient-service: building a {@link Practitioner} from a request goes
 * through its constructor by hand in the service, to keep its validation.
 */
@Mapper(componentModel = "spring")
public interface PractitionerMapper {

    PractitionerResponse toResponse(Practitioner practitioner);
}
