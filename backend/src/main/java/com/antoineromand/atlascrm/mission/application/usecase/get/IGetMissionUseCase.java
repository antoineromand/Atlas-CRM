package com.antoineromand.atlascrm.mission.application.usecase.get;

import com.antoineromand.atlascrm.mission.domain.Mission;
import java.util.UUID;

public interface IGetMissionUseCase {
  Mission execute(UUID accountId, UUID missionId);
}
