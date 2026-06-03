package com.antoineromand.atlascrm.client.application.usecase.update;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateClientUseCase implements IUpdateClientUseCase {

  private final IClientRepository clientRepository;

  public UpdateClientUseCase(IClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Override
  public Client execute(UUID accountId, UUID clientId, UpdateClientCommand command) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    Client existing =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () ->
                    new ClientNotFoundException(
                        "CLIENT_NOT_FOUND", "The client does not exist."));

    Client updated =
        new Client(
            existing.getId(),
            existing.getAccountId(),
            this.resolveCompanyName(command == null ? null : command.companyName(), existing.getCompanyName()),
            this.resolveStatus(command == null ? null : command.status(), existing.getStatus()),
            this.resolveNotes(command == null ? null : command.notes(), existing.getNotes()),
            existing.getCreatedAt(),
            Instant.now());

    this.clientRepository.save(updated);
    return this.clientRepository
        .findByIdAndAccountId(clientId, accountId)
        .orElseThrow(() -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));
  }

  private String resolveCompanyName(
      PatchValue<String> patchValue, String currentValue) {
    if (patchValue == null || !patchValue.present()) {
      return currentValue;
    }

    String value = patchValue.value();
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("companyName must not be blank");
    }

    return value.trim();
  }

  private String resolveStatus(
      PatchValue<String> patchValue, String currentValue) {
    if (patchValue == null || !patchValue.present()) {
      return currentValue;
    }

    String value = patchValue.value();
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("status must not be blank");
    }

    String normalized = value.trim().toLowerCase();
    switch (normalized) {
      case "prospect", "active", "inactive", "archived" -> {
        return normalized;
      }
      default -> throw new IllegalArgumentException("status must be one of: prospect, active, inactive, archived");
    }
  }

  private String resolveNotes(
      PatchValue<String> patchValue, String currentValue) {
    if (patchValue == null || !patchValue.present()) {
      return currentValue;
    }

    String value = patchValue.value();
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
