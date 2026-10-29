package com.brandonkamga.sourcefolio.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for every {@code *IT}: real PostgreSQL, real Flyway migrations, Hibernate
 * validating the mapping, exactly as in production.
 *
 * The container is started once per JVM in a static block, not as a JUnit {@code @Container}:
 * JUnit would stop it after each class while Spring's cached context keeps pointing at it. One
 * database for the whole suite means state outlives a class, so isolation is the subclass's
 * job: {@code @Transactional} on the test class, or an explicit cleanup when the code commits.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class PostgresIT {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    static {
        POSTGRES.start();
    }
}
