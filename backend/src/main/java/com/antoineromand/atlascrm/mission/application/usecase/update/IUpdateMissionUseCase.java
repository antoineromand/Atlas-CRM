package com.antoineromand.atlascrm.mission.application.usecase.update;

import com.antoineromand.atlascrm.mission.domain.Mission;
import java.util.UUID;

public interface IUpdateMissionUseCase {
  Mission execute(UUID accountId, UUID missionId, UpdateMissionCommand command);
}
