package com.example.partystarter.service.identity;

import com.example.partystarter.model.User;

public record AuthenticatedUser(User user) implements CallerIdentity {}
