package com.antoineromand.atlascrm.api.client.dto;

import java.time.Instant;
import java.util.UUID;

public record ClientActivityResponseDto(
    UUID id,
    String activityType,
    String title,
    String description,
    Instant occurredAt,
    Instant createdAt,
    Instant updatedAt) {}
