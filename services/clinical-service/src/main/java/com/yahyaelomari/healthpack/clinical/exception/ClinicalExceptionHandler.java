package com.yahyaelomari.healthpack.clinical.api;

import com.yahyaelomari.healthpack.clinical.exception.ConditionNotFoundException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterClosedException;
import com.yahyaelomari.healthpack.clinical.exception.EncounterNotFoundException;
import com.yahyaelomari.healthpack.clinical.exception.PatientNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Same shape as scheduling-service's {@code SchedulingExceptionHandler}:
 * this stays local rather than moving to {@code healthpack-common} until the
 * duplication is worth removing.
 */
@RestControllerAdvice
public class ClinicalExceptionHandler {

    @ExceptionHandler(EncounterNotFoundException.class)
    public ProblemDetail handleEncounterNotFound(EncounterNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConditionNotFoundException.class)
    public ProblemDetail handleConditionNotFound(ConditionNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handlePatientNotFound(PatientNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EncounterClosedException.class)
    public ProblemDetail handleEncounterClosed(EncounterClosedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    // Bean validation can't see that endedAt must be after the stored
    // startedAt, so Encounter.finish() rejects it and this turns that into
    // a 400 instead of a 500.
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}