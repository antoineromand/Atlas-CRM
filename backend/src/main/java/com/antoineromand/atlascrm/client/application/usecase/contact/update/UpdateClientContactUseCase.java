package com.antoineromand.atlascrm.client.application.usecase.contact.update;

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
public class UpdateClientContactUseCase implements IUpdateClientContactUseCase {

  private final IClientRepository clientRepository;
  private final IClientContactRepository clientContactRepository;

  public UpdateClientContactUseCase(
      IClientRepository clientRepository, IClientContactRepository clientContactRepository) {
    this.clientRepository = clientRepository;
    this.clientContactRepository = clientContactRepository;
  }

  @Override
  public ClientContact execute(
      UUID accountId, UUID clientId, UUID contactId, UpdateClientContactCommand command) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    if (contactId == null) {
      throw new IllegalArgumentException("contactId must not be null");
    }

    if (command == null) {
      throw new IllegalArgumentException("command must not be null");
    }

    Client existingClient =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));

    ClientContact existingContact =
        this.clientContactRepository
            .findById(contactId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_CONTACT_NOT_FOUND", "The contact does not exist."));

    if (!existingClient.getId().equals(existingContact.getClientId())) {
      throw new ClientNotFoundException("CLIENT_CONTACT_NOT_FOUND", "The contact does not exist.");
    }

    ClientContact updated =
        new ClientContact(
            existingContact.getId(),
            existingContact.getClientId(),
            this.resolve(command.firstName(), existingContact.getFirstName(), "firstName"),
            this.resolve(command.lastName(), existingContact.getLastName(), "lastName"),
            this.resolveNullable(command.email(), existingContact.getEmail()),
            this.resolveNullable(command.phone(), existingContact.getPhone()),
            this.resolveNullable(command.jobTitle(), existingContact.getJobTitle()),
            command.primary(),
            existingContact.getCreatedAt(),
            Instant.now());
    this.clientContactRepository.save(updated);
    return this.clientContactRepository
        .findById(contactId)
        .orElseThrow(
            () -> new ClientNotFoundException("CLIENT_CONTACT_NOT_FOUND", "The contact does not exist."));
  }

  private String resolve(String value, String currentValue, String fieldName) {
    String normalized = this.normalize(value);
    if (normalized == null) {
      if (currentValue == null || currentValue.isBlank()) {
        throw new IllegalArgumentException(fieldName + " must not be blank");
      }
      return currentValue;
    }
    return normalized;
  }

  private String resolveNullable(String value, String currentValue) {
    if (value == null) {
      return currentValue;
    }

    String normalized = this.normalize(value);
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
