package com.antoineromand.atlascrm.mission.application.usecase.delete;

import com.antoineromand.atlascrm.mission.application.exceptions.MissionNotFoundException;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteMissionUseCase implements IDeleteMissionUseCase {

  private final IMissionRepository missionRepository;

  public DeleteMissionUseCase(IMissionRepository missionRepository) {
    this.missionRepository = missionRepository;
  }

  @Override
  public void execute(UUID accountId, UUID missionId) {
    this.missionRepository
        .findByIdAndAccountId(missionId, accountId)
        .orElseThrow(() -> new MissionNotFoundException("MISSION_NOT_FOUND", "The mission does not exist."));
    this.missionRepository.deleteById(missionId);
  }
}
