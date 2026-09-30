package com.yahyaelomari.healthpack.patient.api;

import com.yahyaelomari.healthpack.patient.exception.PatientNotFoundException;
import com.yahyaelomari.healthpack.patient.exception.PatientVersionConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Turns exceptions the service layer throws into real HTTP responses.
 *
 * <p>This lives in patient-service, not in {@code healthpack-common}, because
 * two of these four handlers are patient-specific. Once a second service
 * needs the same generic handling — validation errors, a duplicate-key
 * conflict — that shared part is worth moving up. One service isn't.
 */
@RestControllerAdvice
public class PatientExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handleNotFound(PatientNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PatientVersionConflictException.class)
    public ProblemDetail handleVersionConflict(PatientVersionConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Belt-and-suspenders: MrnGenerator should make a duplicate MRN
    // unreachable, but uk_patient_mrn from V1__patient.sql is the real
    // backstop, and this is what stops its violation surfacing as a raw 500.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDuplicateKey(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "A patient with this MRN already exists");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
