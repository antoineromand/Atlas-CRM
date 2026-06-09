package com.antoineromand.atlascrm.mission.application.usecase.create;

import java.time.LocalDate;
import java.util.UUID;

public record CreateMissionCommand(
    UUID accountId,
    UUID clientId,
    String title,
    String roleInProject,
    String description,
    String status,
    String priority,
    LocalDate startDate,
    LocalDate deadline) {}
