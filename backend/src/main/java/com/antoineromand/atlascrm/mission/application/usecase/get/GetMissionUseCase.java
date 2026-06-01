package com.antoineromand.atlascrm.mission.application.usecase.get;

import com.antoineromand.atlascrm.mission.application.exceptions.MissionNotFoundException;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetMissionUseCase implements IGetMissionUseCase {

  private final IMissionRepository missionRepository;

  public GetMissionUseCase(IMissionRepository missionRepository) {
    this.missionRepository = missionRepository;
  }

  @Override
  public Mission execute(UUID accountId, UUID missionId) {
    return this.missionRepository
        .findByIdAndAccountId(missionId, accountId)
        .orElseThrow(() -> new MissionNotFoundException("MISSION_NOT_FOUND", "The mission does not exist."));
  }
}
