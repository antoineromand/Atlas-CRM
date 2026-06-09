package com.antoineromand.atlascrm.mission.application.usecase.create;

import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionCreationException;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.MissionStatus;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateMissionUseCase implements ICreateMissionUseCase {

  private final IMissionRepository missionRepository;
  private final IAccountRepository accountRepository;

  public CreateMissionUseCase(
      IMissionRepository missionRepository, IAccountRepository accountRepository) {
    this.missionRepository = missionRepository;
    this.accountRepository = accountRepository;
  }

  @Override
  public java.util.UUID execute(CreateMissionCommand command) {
    Account account =
        this.accountRepository
            .findById(command.accountId())
            .orElseThrow(
                () ->
                    new MissionCreationException(
                        "ACCOUNT_NOT_FOUND", "The selected account does not exist."));

    Mission mission =
        new Mission(
            null,
            account.getId(),
            command.title(),
            command.roleInProject(),
            command.description(),
            this.resolveStatus(command.status()),
            this.resolvePriority(command.priority()),
            command.startDate(),
            command.deadline(),
            Instant.now(),
            null);

    return this.missionRepository.save(mission);
  }

  private String resolveStatus(String status) {
    return status != null ? status : MissionStatus.CREATED.value();
  }

  private String resolvePriority(String priority) {
    return priority != null ? priority : "medium";
  }
}
