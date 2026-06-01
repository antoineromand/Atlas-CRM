package com.antoineromand.atlascrm.mission.application.usecase.delete;

import java.util.UUID;

public interface IDeleteMissionUseCase {
  void execute(UUID accountId, UUID missionId);
}
