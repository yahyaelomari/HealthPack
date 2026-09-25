package com.yahyaelomari.healthpack.patient.service;

import com.yahyaelomari.healthpack.patient.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unlike {@code @DataJpaTest}, {@code @JdbcTest} still defaults to replacing
 * the {@code DataSource} with an embedded one — {@code Replace.NONE} keeps
 * the Testcontainers Postgres from {@link AbstractIntegrationTest} instead.
 */
// The JDBC test slice excludes plain @Component beans by default (it only
// auto-detects JDBC-flavoured ones); MrnGenerator has to be pulled in explicitly.
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MrnGenerator.class)
class MrnGeneratorTest extends AbstractIntegrationTest {

    private static final Pattern MRN_PATTERN = Pattern.compile("^P-\\d{4}-\\d{6}$");

    @Autowired
    private MrnGenerator mrnGenerator;

    @Test
    void generatesAnMrnMatchingTheExpectedFormat() {
        assertThat(mrnGenerator.generate()).matches(MRN_PATTERN);
    }

    @Test
    void concurrentGenerationNeverProducesTheSameMrn() throws Exception {
        int callers = 50;
        ExecutorService pool = Executors.newFixedThreadPool(callers);

        try {
            List<Future<String>> futures = IntStream.range(0, callers)
                    .mapToObj(i -> pool.submit(mrnGenerator::generate))
                    .toList();

            Set<String> mrns = futures.stream()
                    .map(this::getUnchecked)
                    .collect(Collectors.toSet());

            // A Set collapses duplicates, so this is exactly the check that
            // matters: if two callers ever received the same value, the
            // sizes would diverge here.
            assertThat(mrns).hasSize(callers);
            assertThat(mrns).allMatch(mrn -> MRN_PATTERN.matcher(mrn).matches());
        } finally {
            pool.shutdown();
        }
    }

    private String getUnchecked(Future<String> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
