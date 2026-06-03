package com.antoineromand.atlascrm.client.application.usecase.get;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.ClientTag;
import com.antoineromand.atlascrm.client.domain.repository.IClientActivityRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientContactRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientTagRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetClientUseCase implements IGetClientUseCase {

  private final IClientRepository clientRepository;
  private final IClientContactRepository clientContactRepository;
  private final IClientActivityRepository clientActivityRepository;
  private final IClientTagRepository clientTagRepository;

  public GetClientUseCase(
      IClientRepository clientRepository,
      IClientContactRepository clientContactRepository,
      IClientActivityRepository clientActivityRepository,
      IClientTagRepository clientTagRepository) {
    this.clientRepository = clientRepository;
    this.clientContactRepository = clientContactRepository;
    this.clientActivityRepository = clientActivityRepository;
    this.clientTagRepository = clientTagRepository;
  }

  @Override
  public ClientDetailResult execute(UUID accountId, UUID clientId) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    var client =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));

    List<ClientContact> contacts = this.clientContactRepository.findAllByClientId(clientId);
    List<ClientActivity> activities = this.clientActivityRepository.findAllByClientId(clientId);
    List<ClientTag> tags = this.clientTagRepository.findAllByClientId(clientId);

    return new ClientDetailResult(client, contacts, activities, tags);
  }
}
