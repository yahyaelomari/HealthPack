package com.yahyaelomari.healthpack.clinical.mapper;


import com.yahyaelomari.healthpack.clinical.api.dto.EncounterResponse;
import com.yahyaelomari.healthpack.clinical.domain.Encounter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EncounterMapper {
    EncounterResponse toResponse(Encounter encounter);
}
