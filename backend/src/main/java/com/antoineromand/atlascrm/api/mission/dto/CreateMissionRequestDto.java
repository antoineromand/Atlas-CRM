package com.antoineromand.atlascrm.api.mission.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateMissionRequestDto(
    String title,
    String roleInProject,
    String description,
    UUID clientId,
    String status,
    String priority,
    LocalDate startDate,
    LocalDate deadline) {}
