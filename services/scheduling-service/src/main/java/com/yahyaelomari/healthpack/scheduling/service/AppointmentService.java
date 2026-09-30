package com.yahyaelomari.healthpack.scheduling.service;

import com.yahyaelomari.healthpack.scheduling.api.dto.AppointmentResponse;
import com.yahyaelomari.healthpack.scheduling.api.dto.BookAppointmentRequest;
import com.yahyaelomari.healthpack.scheduling.api.dto.RescheduleAppointmentRequest;
import com.yahyaelomari.healthpack.scheduling.domain.Appointment;
import com.yahyaelomari.healthpack.scheduling.exception.AppointmentNotFoundException;
import com.yahyaelomari.healthpack.scheduling.exception.AppointmentVersionConflictException;
import com.yahyaelomari.healthpack.scheduling.exception.PatientNotFoundException;
import com.yahyaelomari.healthpack.scheduling.exception.SlotUnavailableException;
import com.yahyaelomari.healthpack.scheduling.grpc.PatientClient;
import com.yahyaelomari.healthpack.scheduling.mapper.AppointmentMapper;
import com.yahyaelomari.healthpack.scheduling.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final PatientClient patientClient;

    public AppointmentResponse book(BookAppointmentRequest request) {
        if (!patientClient.exists(request.patientId())) {
            throw new PatientNotFoundException(request.patientId());
        }

        Appointment appointment = Appointment.builder()
                .practitionerId(request.practitionerId())
                .patientId(request.patientId())
                .slotStart(request.slotStart())
                .slotEnd(request.slotEnd())
                .reason(request.reason())
                .build();

        return appointmentMapper.toResponse(saveOrThrowIfSlotTaken(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        return appointmentMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByPatientId(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    public AppointmentResponse confirm(UUID id) {
        Appointment appointment = getOrThrow(id);
        appointment.confirm();
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse cancel(UUID id, String reason) {
        Appointment appointment = getOrThrow(id);
        appointment.cancel(reason);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse complete(UUID id) {
        Appointment appointment = getOrThrow(id);
        appointment.complete();
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse reschedule(UUID id, RescheduleAppointmentRequest request, long expectedVersion) {
        Appointment appointment = getOrThrow(id);

        if (appointment.getVersion() != expectedVersion) {
            throw new AppointmentVersionConflictException();
        }

        appointment.reschedule(request.slotStart(), request.slotEnd());
        return appointmentMapper.toResponse(saveOrThrowIfSlotTaken(appointment));
    }

    private Appointment getOrThrow(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    // Shared by book() and reschedule(): both write a slotStart/slotEnd that
    // no_double_booking (V1__scheduling.sql) checks, and both can lose that
    // race the same two ways — see SlotUnavailableException's javadoc.
    private Appointment saveOrThrowIfSlotTaken(Appointment appointment) {
        try {
            return appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException | CannotAcquireLockException e) {
            throw new SlotUnavailableException();
        }
    }
}
