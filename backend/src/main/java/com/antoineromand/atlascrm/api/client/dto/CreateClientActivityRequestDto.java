package com.antoineromand.atlascrm.api.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateClientActivityRequestDto(
    @NotBlank @Size(max = 32) String activityType,
    @NotBlank @Size(max = 200) String title,
    @Size(max = 4000) String description,
    @NotNull Instant occurredAt) {}
