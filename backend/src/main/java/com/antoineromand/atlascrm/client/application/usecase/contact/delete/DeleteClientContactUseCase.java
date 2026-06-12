package com.antoineromand.atlascrm.client.application.usecase.contact.delete;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.repository.IClientContactRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteClientContactUseCase implements IDeleteClientContactUseCase {

  private final IClientRepository clientRepository;
  private final IClientContactRepository clientContactRepository;

  public DeleteClientContactUseCase(
      IClientRepository clientRepository, IClientContactRepository clientContactRepository) {
    this.clientRepository = clientRepository;
    this.clientContactRepository = clientContactRepository;
  }

  @Override
  public void execute(UUID accountId, UUID clientId, UUID contactId) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    if (contactId == null) {
      throw new IllegalArgumentException("contactId must not be null");
    }

    Client client =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));

    ClientContact contact =
        this.clientContactRepository
            .findById(contactId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_CONTACT_NOT_FOUND", "The contact does not exist."));

    if (!client.getId().equals(contact.getClientId())) {
      throw new ClientNotFoundException("CLIENT_CONTACT_NOT_FOUND", "The contact does not exist.");
    }

    this.clientContactRepository.deleteById(contactId);
  }
}
