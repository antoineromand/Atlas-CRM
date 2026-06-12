package com.antoineromand.atlascrm.api.client.dto;

import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateClientActivityRequestDto(
    @Size(max = 32) String activityType,
    @Size(max = 200) String title,
    @Size(max = 4000) String description,
    Instant occurredAt) {}
