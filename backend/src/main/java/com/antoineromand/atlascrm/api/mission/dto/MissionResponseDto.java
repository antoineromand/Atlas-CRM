package com.antoineromand.atlascrm.api.mission.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MissionResponseDto(
    UUID id,
    String title,
    String roleInProject,
    String description,
    String status,
    String priority,
    LocalDate startDate,
    LocalDate deadline,
    Instant createdAt,
    Instant updatedAt) {}
