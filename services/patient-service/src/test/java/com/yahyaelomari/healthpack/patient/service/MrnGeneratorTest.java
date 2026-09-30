package com.yahyaelomari.healthpack.patient.service;

import com.yahyaelomari.healthpack.patient.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MrnGeneratorTest extends AbstractIntegrationTest {

    @Autowired
    private MrnGenerator mrnGenerator;

    @Test
    void mrnFollowsTheExpectedFormat() {
        String mrn = mrnGenerator.generate();

        assertThat(mrn).matches("P-\\d{4}-\\d{6}");
    }

    @Test
    void twoCallsNeverReturnTheSameMrn() {
        String first = mrnGenerator.generate();
        String second = mrnGenerator.generate();

        assertThat(first).isNotEqualTo(second);
    }
}
