package com.example.partystarter.service.identity;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomDiscriminatorGenerator implements DiscriminatorGenerator {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String next() {
        // 1..9999 inclusive (skip 0000 to keep tags non-zero like Discord)
        int n = random.nextInt(9999) + 1;
        return String.format("%04d", n);
    }
}
