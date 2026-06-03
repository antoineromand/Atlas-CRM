package com.antoineromand.atlascrm.client.application.usecase.delete;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteClientUseCase implements IDeleteClientUseCase {

  private final IClientRepository clientRepository;

  public DeleteClientUseCase(IClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Override
  public void execute(UUID accountId, UUID clientId) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    this.clientRepository
        .findByIdAndAccountId(clientId, accountId)
        .orElseThrow(
            () ->
                new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));
    this.clientRepository.deleteById(clientId);
  }
}
