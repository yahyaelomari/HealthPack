package com.yahyaelomari.healthpack.patient.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for any test that needs a real Postgres.
 *
 * <p>{@code @ServiceConnection} wires the container's JDBC URL, username and
 * password into Spring's {@code DataSource} automatically — no manual
 * {@code @DynamicPropertySource} needed. One container is started per test JVM
 * and reused across every test class that extends this one, which is why the
 * field is {@code static}.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
}
