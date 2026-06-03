package com.antoineromand.atlascrm.api.client.dto;

import java.time.Instant;
import java.util.UUID;

public record ClientContactResponseDto(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String jobTitle,
    boolean primary,
    Instant createdAt,
    Instant updatedAt) {}
