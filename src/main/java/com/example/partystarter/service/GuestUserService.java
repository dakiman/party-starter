package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.GuestUser;
import com.example.partystarter.repo.GuestUserRepository;
import com.example.partystarter.service.identity.DiscriminatorGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GuestUserService {

    private static final int MAX_DISPLAY_NAME_LEN = 64;

    private final GuestUserRepository repository;
    private final DiscriminatorGenerator discriminatorGenerator;
    private final GuestUserService self;

    public GuestUserService(GuestUserRepository repository,
                            DiscriminatorGenerator discriminatorGenerator,
                            @Lazy @Autowired GuestUserService self) {
        this.repository = repository;
        this.discriminatorGenerator = discriminatorGenerator;
        this.self = self;
    }

    /**
     * Creates a new GuestUser with a freshly generated guest_token + discriminator.
     * Retries once on (display_name, discriminator) collision before giving up.
     * Caller is responsible for trimming/validating display name; we trim leading/trailing
     * whitespace and reject empty strings here as a backstop.
     *
     * <p>Each persistence attempt runs in its own transaction
     * ({@link Propagation#REQUIRES_NEW} via {@link #tryCreate}) — once a
     * {@code saveAndFlush} throws on a uniqueness collision, Hibernate marks
     * the session for rollback and refuses to flush again. Splitting attempts
     * into independent transactions keeps the retry path functional.
     */
    public GuestUser createNew(String displayName, String contactNote) {
        String trimmedName = displayName == null ? "" : displayName.trim();
        if (trimmedName.isEmpty()) {
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Display name is required");
        }
        if (trimmedName.length() > MAX_DISPLAY_NAME_LEN) {
            throw new ResourceException(HttpStatus.BAD_REQUEST,
                "Display name must be " + MAX_DISPLAY_NAME_LEN + " characters or fewer");
        }

        try {
            return self.tryCreate(trimmedName, contactNote);
        } catch (DataIntegrityViolationException firstCollision) {
            try {
                return self.tryCreate(trimmedName, contactNote);
            } catch (DataIntegrityViolationException secondCollision) {
                throw new ResourceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Name unavailable, try a different name.");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GuestUser tryCreate(String displayName, String contactNote) {
        return repository.saveAndFlush(GuestUser.builder()
            .displayName(displayName)
            .discriminator(discriminatorGenerator.next())
            .contactNote(contactNote)
            .guestToken(UUID.randomUUID().toString())
            .build());
    }
}
