package com.example.partystarter.service.identity;

/**
 * Generates a 4-digit zero-padded discriminator string ("0001" through "9999").
 * Pulled behind an interface so tests can stub deterministic colliding values.
 */
public interface DiscriminatorGenerator {
    String next();
}
