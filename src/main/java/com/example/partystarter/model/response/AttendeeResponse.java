package com.example.partystarter.model.response;

public record AttendeeResponse(Long id, AttendeeIdentity identity, String status) {
    public record AttendeeIdentity(
        String kind,
        String displayName,
        String discriminator,
        String username
    ) {}
}
