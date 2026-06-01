package com.antoineromand.atlascrm.api.mission.dto;

import java.time.LocalDate;

public record CreateMissionRequestDto(
    String title,
    String roleInProject,
    String description,
    String status,
    String priority,
    LocalDate startDate,
    LocalDate deadline) {}
