package com.antoineromand.atlascrm.mission.application.usecase.summary;

import java.util.UUID;

public interface IGetMissionSummaryUseCase {
  MissionSummaryResult execute(UUID accountId);
}
