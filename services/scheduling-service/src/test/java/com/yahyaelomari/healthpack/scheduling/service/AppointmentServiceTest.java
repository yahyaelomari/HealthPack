package com.yahyaelomari.healthpack.scheduling.service;

import com.yahyaelomari.healthpack.scheduling.api.dto.BookAppointmentRequest;
import com.yahyaelomari.healthpack.scheduling.api.dto.RescheduleAppointmentRequest;
import com.yahyaelomari.healthpack.scheduling.domain.Appointment;
import com.yahyaelomari.healthpack.scheduling.exception.AppointmentNotFoundException;
import com.yahyaelomari.healthpack.scheduling.exception.AppointmentVersionConflictException;
import com.yahyaelomari.healthpack.scheduling.exception.SlotUnavailableException;
import com.yahyaelomari.healthpack.scheduling.mapper.AppointmentMapper;
import com.yahyaelomari.healthpack.scheduling.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The two "loses the race" tests are the point of this class: proving
 * {@link AppointmentService} translates both of no_double_booking's failure
 * modes — found the hard way in Feat #13 — into the one exception a caller
 * actually wants to see.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void bookThrowsSlotUnavailableOnAConstraintViolation() {
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("no_double_booking"));

        assertThatThrownBy(() -> appointmentService.book(newBookRequest()))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    void bookThrowsSlotUnavailableOnADeadlock() {
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenThrow(new CannotAcquireLockException("deadlock detected"));

        assertThatThrownBy(() -> appointmentService.book(newBookRequest()))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    void findByIdThrowsWhenTheAppointmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.findById(id))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test
    void rescheduleThrowsWhenTheCallerHasAStaleVersion() {
        UUID id = UUID.randomUUID();
        Appointment existing = Appointment.builder()
                .practitionerId(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .slotStart(Instant.now().plusSeconds(3600))
                .slotEnd(Instant.now().plusSeconds(5400))
                .build();
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(existing));

        long staleVersion = existing.getVersion() + 1;
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(
                Instant.now().plusSeconds(7200), Instant.now().plusSeconds(9000));

        assertThatThrownBy(() -> appointmentService.reschedule(id, request, staleVersion))
                .isInstanceOf(AppointmentVersionConflictException.class);
    }

    private BookAppointmentRequest newBookRequest() {
        return new BookAppointmentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now().plusSeconds(3600),
                Instant.now().plusSeconds(5400),
                "check-up");
    }
}
