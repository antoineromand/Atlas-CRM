package com.antoineromand.atlascrm.api.client.dto;

import java.time.Instant;
import java.util.UUID;

public record ClientTagResponseDto(
    UUID id,
    String name,
    String color,
    Instant createdAt,
    Instant updatedAt) {}
