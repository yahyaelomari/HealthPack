package com.yahyaelomari.healthpack.scheduling.mapper;

import com.yahyaelomari.healthpack.scheduling.api.dto.AppointmentResponse;
import com.yahyaelomari.healthpack.scheduling.domain.Appointment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    AppointmentResponse toResponse(Appointment appointment);
}
