package com.yahyaelomari.healthpack.scheduling.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * A singleton container — started once for the whole test run, not managed
 * per test class.
 *
 * <p>{@code @Testcontainers} + {@code @Container} stops the container after
 * the owning test class finishes. That's fine when only one class extends
 * this — it broke the moment a second one did: Testcontainers quietly
 * restarted the (shared, inherited) container on a new port for the second
 * class, while Spring's test context cache — seeing two identical
 * {@code @DataJpaTest} signatures — reused the first class's already-built
 * context, still wired to the old, now-dead port. Two systems disagreeing
 * about which container is current, surfacing as plain "connection refused."
 *
 * <p>Starting it once here, in a static initializer, with nothing ever
 * calling {@code stop()}, is the standard fix: one container for the entire
 * test run, cleaned up by Ryuk at JVM exit rather than per class.
 * {@code @ServiceConnection} still does its job — that annotation is how
 * Spring discovers the connection details, independent of whether JUnit's
 * {@code @Testcontainers} extension manages the start/stop lifecycle.
 */
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }
}
