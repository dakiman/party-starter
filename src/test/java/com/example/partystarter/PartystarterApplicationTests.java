package com.example.partystarter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot scaffold context-loads test. Disabled because:
 *   - Bare {@code @SpringBootTest} has no DataSource available during {@code mvn test}
 *     (Phase 1 moved schema management to Flyway with {@code ddl-auto: validate}, which
 *     requires an actual MySQL server to be reachable at boot).
 *   - {@link com.example.partystarter.api.EventControllerIntegrationTest} (extending
 *     {@link BaseIntegrationTest}) loads the full Spring context with a Testcontainers
 *     MySQL instance — that's the modern equivalent of "context loads".
 *
 * Kept as a record. Re-enable + extend BaseIntegrationTest if a focused context-load
 * smoke is ever wanted again.
 */
@Disabled("superseded by EventControllerIntegrationTest; see class javadoc")
@SpringBootTest
class PartystarterApplicationTests {

	@Test
	void contextLoads() {
	}

}
