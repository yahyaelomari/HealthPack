package com.yahyaelomari.healthpack.scheduling.api;

import com.yahyaelomari.healthpack.scheduling.exception.AppointmentNotFoundException;
import com.yahyaelomari.healthpack.scheduling.exception.AppointmentVersionConflictException;
import com.yahyaelomari.healthpack.scheduling.exception.PatientNotFoundException;
import com.yahyaelomari.healthpack.scheduling.exception.PractitionerNotFoundException;
import com.yahyaelomari.healthpack.scheduling.exception.SlotUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Same shape as patient-service's {@code PatientExceptionHandler}: this
 * stays local rather than moving to {@code healthpack-common} until a
 * second service needs the same generic handling.
 */
@RestControllerAdvice
public class SchedulingExceptionHandler {

    @ExceptionHandler(PractitionerNotFoundException.class)
    public ProblemDetail handlePractitionerNotFound(PractitionerNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ProblemDetail handleAppointmentNotFound(AppointmentNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handlePatientNotFound(PatientNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // The one exception no_double_booking's two failure modes (a clean
    // violation or a genuine deadlock — see SlotUnavailableException's
    // javadoc) both get translated into, so this handler never needs to
    // know which one actually happened.
    @ExceptionHandler(SlotUnavailableException.class)
    public ProblemDetail handleSlotUnavailable(SlotUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AppointmentVersionConflictException.class)
    public ProblemDetail handleVersionConflict(AppointmentVersionConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
