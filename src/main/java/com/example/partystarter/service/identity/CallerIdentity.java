package com.example.partystarter.service.identity;

public sealed interface CallerIdentity permits AuthenticatedUser, Guest {}
