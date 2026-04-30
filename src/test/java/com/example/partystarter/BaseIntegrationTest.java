package com.example.partystarter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base class for integration tests.
 *
 * Boots the full Spring context against a Testcontainers MySQL 8 instance.
 * Flyway runs the same migrations as production (everything in
 * src/main/resources/db/migration/), so the schema under test is identical
 * to prod's. Subclasses get an autowired {@link MockMvc} for issuing requests
 * and an {@link ObjectMapper} for serialising request bodies.
 *
 * <p><b>JVM-singleton container.</b> The container is started in a static
 * initializer and never explicitly stopped — the JVM (and Testcontainers' Ryuk
 * reaper) shuts it down at process exit. This is intentional: with the
 * {@code @Testcontainers} + {@code @Container} per-class lifecycle, the first
 * test class would stop the container in its {@code @AfterAll}, leaving
 * subsequent classes with a dead JDBC URL ("Connection refused" through the
 * Hikari pool). The singleton pattern lets every {@code BaseIntegrationTest}
 * subclass share one MySQL instance and pays the boot cost once per JVM.
 *
 * <p>Each test should clean up its own data (or use {@code @Transactional}
 * with rollback) to stay isolated across methods AND across classes.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
