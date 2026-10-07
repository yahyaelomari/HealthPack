package com.yahyaelomari.healthpack.clinical.mapper;

import com.yahyaelomari.healthpack.clinical.api.dto.ConditionResponse;
import com.yahyaelomari.healthpack.clinical.domain.Condition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConditionMapper {

    ConditionResponse toResponse(Condition condition);
}
