package com.example.partystarter.service;

import com.example.partystarter.BaseIntegrationTest;
import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.GuestUser;
import com.example.partystarter.repo.GuestUserRepository;
import com.example.partystarter.service.identity.DiscriminatorGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the discriminator-collision retry behavior. Uses a stub
 * DiscriminatorGenerator that returns a deterministic sequence so the
 * test produces predictable collisions instead of relying on random luck.
 */
@Import(GuestUserServiceCollisionTest.StubGeneratorConfig.class)
class GuestUserServiceCollisionTest extends BaseIntegrationTest {

    @Autowired
    private GuestUserService guestUserService;

    @Autowired
    private GuestUserRepository guestUserRepository;

    @Autowired
    private SequenceDiscriminatorGenerator stubGenerator;

    @AfterEach
    void cleanup() {
        guestUserRepository.deleteAll();
        stubGenerator.reset();
    }

    @Test
    @DisplayName("createNew — first-attempt success")
    void firstAttempt_success() {
        stubGenerator.feed("0001");
        GuestUser g = guestUserService.createNew("Alice", null);
        assertEquals("Alice", g.getDisplayName());
        assertEquals("0001", g.getDiscriminator());
    }

    @Test
    @DisplayName("createNew — first attempt collides on (name, disc), second attempt wins")
    void retryOnce_succeedsOnSecond() {
        // Pre-seed an Alice#0001
        guestUserRepository.saveAndFlush(GuestUser.builder()
            .displayName("Alice").discriminator("0001")
            .guestToken("00000000-0000-0000-0000-000000000001")
            .build());

        stubGenerator.feed("0001", "0002");
        GuestUser g = guestUserService.createNew("Alice", null);
        assertEquals("Alice", g.getDisplayName());
        assertEquals("0002", g.getDiscriminator());
    }

    @Test
    @DisplayName("createNew — both attempts collide → 503")
    void doubleCollision_throws503() {
        guestUserRepository.saveAndFlush(GuestUser.builder()
            .displayName("Alice").discriminator("0001")
            .guestToken("00000000-0000-0000-0000-000000000001")
            .build());
        guestUserRepository.saveAndFlush(GuestUser.builder()
            .displayName("Alice").discriminator("0002")
            .guestToken("00000000-0000-0000-0000-000000000002")
            .build());

        stubGenerator.feed("0001", "0002");
        ResourceException ex = assertThrows(ResourceException.class,
            () -> guestUserService.createNew("Alice", null));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getHttpStatus());
    }

    @Test
    @DisplayName("createNew — empty/whitespace display name → 400")
    void blankName_throws400() {
        stubGenerator.feed("0001");
        ResourceException ex = assertThrows(ResourceException.class,
            () -> guestUserService.createNew("   ", null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @TestConfiguration
    static class StubGeneratorConfig {
        @Bean
        @Primary
        public SequenceDiscriminatorGenerator sequenceDiscriminatorGenerator() {
            return new SequenceDiscriminatorGenerator();
        }
    }

    /**
     * Test-only DiscriminatorGenerator that emits a pre-fed sequence of values.
     * Marked @Primary in the @TestConfiguration to override the default
     * SecureRandomDiscriminatorGenerator bean.
     */
    static class SequenceDiscriminatorGenerator implements DiscriminatorGenerator {
        private Iterator<String> queue = List.<String>of().iterator();

        public void feed(String... values) {
            queue = List.of(values).iterator();
        }

        public void reset() {
            queue = List.<String>of().iterator();
        }

        @Override
        public String next() {
            if (!queue.hasNext()) {
                throw new IllegalStateException("Stub generator exhausted — feed more values");
            }
            return queue.next();
        }
    }
}
