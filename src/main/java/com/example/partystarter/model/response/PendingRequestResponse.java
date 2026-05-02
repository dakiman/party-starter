package com.example.partystarter.model.response;

import java.time.LocalDateTime;

public record PendingRequestResponse(
    Long id,
    RequesterIdentity requester,
    LocalDateTime createdAt
) {
    public record RequesterIdentity(
        String kind,
        String displayName,
        String discriminator,
        String contactNote,
        String username
    ) {}
}
