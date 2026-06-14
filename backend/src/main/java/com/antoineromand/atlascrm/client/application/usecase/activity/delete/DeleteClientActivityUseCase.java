package com.antoineromand.atlascrm.client.application.usecase.activity.delete;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.repository.IClientActivityRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteClientActivityUseCase implements IDeleteClientActivityUseCase {

  private final IClientRepository clientRepository;
  private final IClientActivityRepository clientActivityRepository;

  public DeleteClientActivityUseCase(
      IClientRepository clientRepository, IClientActivityRepository clientActivityRepository) {
    this.clientRepository = clientRepository;
    this.clientActivityRepository = clientActivityRepository;
  }

  @Override
  public void execute(UUID accountId, UUID clientId, UUID activityId) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    if (activityId == null) {
      throw new IllegalArgumentException("activityId must not be null");
    }

    Client client =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));

    ClientActivity activity =
        this.clientActivityRepository
            .findById(activityId)
            .orElseThrow(
                () ->
                    new ClientNotFoundException(
                        "CLIENT_ACTIVITY_NOT_FOUND", "The activity does not exist."));

    if (!client.getId().equals(activity.getClientId())) {
      throw new ClientNotFoundException("CLIENT_ACTIVITY_NOT_FOUND", "The activity does not exist.");
    }

    this.clientActivityRepository.deleteById(activityId);
  }
}
