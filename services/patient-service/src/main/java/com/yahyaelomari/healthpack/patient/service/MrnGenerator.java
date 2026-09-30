package com.yahyaelomari.healthpack.patient.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Generates the medical record number shown to staff and patients.
 *
 * <p>Deliberately backed by a Postgres {@code SEQUENCE} rather than a
 * "read the max, add one" scheme in Java: {@code nextval()} is atomic at the
 * database level, so two concurrent registrations can never be handed the
 * same number. Nothing here needs its own locking.
 *
 * <p>A sequence value is never reclaimed, including when the transaction that
 * called {@code nextval()} rolls back — so a failed registration leaves a gap
 * in the numbering rather than a collision. That is the correct trade-off:
 * real hospital MRNs already have gaps, and a gap is harmless where a
 * duplicate is not.
 */
@Component
@RequiredArgsConstructor
public class MrnGenerator {

    private final JdbcTemplate jdbcTemplate;

    //nextval provides a tradeoff for a failed transaction; if the transaction fails, the next mrn sequence request will be the next on
    //so if it should give 5 and it failed, the retry gives 6 and not 5, this is better than having 2 patients with the same mrn
    public String generate() {
        Long next = jdbcTemplate.queryForObject("SELECT nextval('patient_mrn_seq')", Long.class);
        return "P-%d-%06d".formatted(Year.now().getValue(), next);
    }
}
