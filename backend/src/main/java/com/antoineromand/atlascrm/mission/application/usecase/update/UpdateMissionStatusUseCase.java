package com.antoineromand.atlascrm.mission.application.usecase.update;

import com.antoineromand.atlascrm.mission.application.exceptions.MissionNotFoundException;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionUpdateException;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.MissionStatus;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateMissionStatusUseCase implements IUpdateMissionStatusUseCase {

  private final IMissionRepository missionRepository;

  public UpdateMissionStatusUseCase(IMissionRepository missionRepository) {
    this.missionRepository = missionRepository;
  }

  @Override
  public Mission execute(UpdateMissionStatusCommand command) {
    Mission existing =
        this.missionRepository
            .findByIdAndAccountId(command.missionId(), command.accountId())
            .orElseThrow(() -> new MissionNotFoundException("MISSION_NOT_FOUND", "The mission does not exist."));

    MissionStatus currentStatus = existing.getMissionStatus();
    MissionStatus nextStatus = resolveStatus(command.status());

    if (!currentStatus.canTransitionTo(nextStatus)) {
      throw new MissionUpdateException("MISSION_STATUS_INVALID_TRANSITION", "Mission status can only move one step at a time.");
    }

    Mission updated =
        new Mission(
            existing.getId(),
            existing.getAccountId(),
            existing.getClientId(),
            existing.getTitle(),
            existing.getRoleInProject(),
            existing.getDescription(),
            nextStatus.value(),
            existing.getPriority(),
            existing.getStartDate(),
            existing.getDeadline(),
            existing.getCreatedAt(),
            Instant.now());

    this.missionRepository.save(updated);
    return this.missionRepository
        .findByIdAndAccountId(command.missionId(), command.accountId())
        .orElseThrow(() -> new MissionNotFoundException("MISSION_NOT_FOUND", "The mission does not exist."));
  }

  private MissionStatus resolveStatus(String status) {
    if (status == null || status.isBlank()) {
      throw new MissionUpdateException("MISSION_STATUS_INVALID_TRANSITION", "Mission status must be a non-empty value.");
    }

    return MissionStatus.fromValue(status);
  }
}
