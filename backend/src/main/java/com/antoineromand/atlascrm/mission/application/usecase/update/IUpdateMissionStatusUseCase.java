package com.antoineromand.atlascrm.mission.application.usecase.update;

import com.antoineromand.atlascrm.mission.domain.Mission;

public interface IUpdateMissionStatusUseCase {
  Mission execute(UpdateMissionStatusCommand command);
}
