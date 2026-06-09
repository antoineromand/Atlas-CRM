package com.antoineromand.atlascrm.mission.application.usecase.create;

import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionCreationException;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.MissionStatus;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateMissionUseCase implements ICreateMissionUseCase {

  private final IMissionRepository missionRepository;
  private final IAccountRepository accountRepository;
  private final IClientRepository clientRepository;

  public CreateMissionUseCase(
      IMissionRepository missionRepository,
      IAccountRepository accountRepository,
      IClientRepository clientRepository) {
    this.missionRepository = missionRepository;
    this.accountRepository = accountRepository;
    this.clientRepository = clientRepository;
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
            this.resolveClientId(command.clientId(), account.getId()),
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

  private UUID resolveClientId(UUID clientId, UUID accountId) {
    if (clientId == null) {
      return null;
    }

    return this.clientRepository
        .findByIdAndAccountId(clientId, accountId)
        .map(client -> client.getId())
        .orElseThrow(() -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));
  }

  private String resolvePriority(String priority) {
    return priority != null ? priority : "medium";
  }
}
