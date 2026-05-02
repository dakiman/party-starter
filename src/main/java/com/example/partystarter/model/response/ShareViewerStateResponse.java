package com.example.partystarter.model.response;

import java.time.LocalDateTime;

public record ShareViewerStateResponse(
    String state,
    String attendeeStatus,
    LocalDateTime requestDecidedAt,
    Boolean isCreator,
    Boolean eventIsPrivate
) {}
