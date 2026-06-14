package com.antoineromand.atlascrm.mission.application.usecase.update;

import java.util.UUID;

public record UpdateMissionStatusCommand(UUID accountId, UUID missionId, String status) {}
