package com.yahyaelomari.healthpack.clinical.mapper;

import com.yahyaelomari.healthpack.clinical.api.dto.ObservationResponse;
import com.yahyaelomari.healthpack.clinical.domain.Observation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ObservationMapper {

    ObservationResponse toResponse(Observation observation);
}