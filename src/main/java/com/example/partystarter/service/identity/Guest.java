package com.example.partystarter.service.identity;

import com.example.partystarter.model.GuestUser;

public record Guest(GuestUser guestUser) implements CallerIdentity {}
