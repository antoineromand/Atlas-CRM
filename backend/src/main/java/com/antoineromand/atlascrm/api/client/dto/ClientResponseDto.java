package com.antoineromand.atlascrm.api.client.dto;

import java.time.Instant;
import java.util.UUID;

public record ClientResponseDto(
    UUID id,
    String companyName,
    String status,
    String notes,
    Instant createdAt,
    Instant updatedAt) {}
