package com.antoineromand.atlascrm.client.application.usecase.contact.create;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.repository.IClientContactRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateClientContactUseCase implements ICreateClientContactUseCase {

  private final IClientRepository clientRepository;
  private final IClientContactRepository clientContactRepository;

  public CreateClientContactUseCase(
      IClientRepository clientRepository, IClientContactRepository clientContactRepository) {
    this.clientRepository = clientRepository;
    this.clientContactRepository = clientContactRepository;
  }

  @Override
  public ClientContact execute(UUID accountId, UUID clientId, CreateClientContactCommand command) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    if (command == null) {
      throw new IllegalArgumentException("command must not be null");
    }

    Client client =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));

    ClientContact contact =
        new ClientContact(
            null,
            client.getId(),
            this.requireValue(command.firstName(), "firstName"),
            this.requireValue(command.lastName(), "lastName"),
            this.normalize(command.email()),
            this.normalize(command.phone()),
            this.normalize(command.jobTitle()),
            command.primary(),
            Instant.now(),
            null);

    UUID contactId = this.clientContactRepository.save(contact);
    return this.clientContactRepository
        .findById(contactId)
        .orElseThrow(
            () ->
                new ClientNotFoundException(
                    "CLIENT_CONTACT_NOT_FOUND", "The contact does not exist."));
  }

  private String requireValue(String value, String fieldName) {
    String normalized = this.normalize(value);
    if (normalized == null) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }

    return normalized;
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
