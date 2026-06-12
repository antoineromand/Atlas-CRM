package com.antoineromand.atlascrm.mission.application.usecase.update;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionNotFoundException;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateMissionUseCase implements IUpdateMissionUseCase {

  private final IMissionRepository missionRepository;
  private final IClientRepository clientRepository;

  public UpdateMissionUseCase(IMissionRepository missionRepository, IClientRepository clientRepository) {
    this.missionRepository = missionRepository;
    this.clientRepository = clientRepository;
  }

  @Override
  public Mission execute(UUID accountId, UUID missionId, UpdateMissionCommand command) {
    Mission existing =
        this.missionRepository
            .findByIdAndAccountId(missionId, accountId)
            .orElseThrow(() -> new MissionNotFoundException("MISSION_NOT_FOUND", "The mission does not exist."));

    Mission updated =
        new Mission(
            existing.getId(),
            existing.getAccountId(),
            resolveClientId(command.clientId(), existing.getClientId(), accountId),
            resolve(command.title(), existing.getTitle()),
            resolve(command.roleInProject(), existing.getRoleInProject()),
            resolve(command.description(), existing.getDescription()),
            existing.getMissionStatus(),
            resolve(command.priority(), existing.getPriority()),
            resolve(command.startDate(), existing.getStartDate()),
            resolve(command.deadline(), existing.getDeadline()),
            existing.getCreatedAt(),
            Instant.now());

    this.missionRepository.save(updated);
    return this.missionRepository
        .findByIdAndAccountId(missionId, accountId)
        .orElseThrow(() -> new MissionNotFoundException("MISSION_NOT_FOUND", "The mission does not exist."));
  }

  private <T> T resolve(com.antoineromand.atlascrm.account.application.usecase.account.PatchValue<T> patchValue, T currentValue) {
    if (patchValue == null || !patchValue.present()) {
      return currentValue;
    }
    return patchValue.value();
  }

  private UUID resolveClientId(
      com.antoineromand.atlascrm.account.application.usecase.account.PatchValue<UUID> patchValue,
      UUID currentValue,
      UUID accountId) {
    if (patchValue == null || !patchValue.present()) {
      return currentValue;
    }

    UUID nextClientId = patchValue.value();
    if (nextClientId == null) {
      return null;
    }

    return this.clientRepository
        .findByIdAndAccountId(nextClientId, accountId)
        .map(client -> client.getId())
        .orElseThrow(() -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));
  }
}
