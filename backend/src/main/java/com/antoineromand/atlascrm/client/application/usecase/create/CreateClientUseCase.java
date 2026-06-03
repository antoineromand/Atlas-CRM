package com.antoineromand.atlascrm.client.application.usecase.create;

import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import com.antoineromand.atlascrm.client.application.exceptions.ClientCreationException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateClientUseCase implements ICreateClientUseCase {

  private final IClientRepository clientRepository;
  private final IAccountRepository accountRepository;

  public CreateClientUseCase(
      IClientRepository clientRepository, IAccountRepository accountRepository) {
    this.clientRepository = clientRepository;
    this.accountRepository = accountRepository;
  }

  @Override
  public java.util.UUID execute(CreateClientCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("command must not be null");
    }

    if (command.accountId() == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    Account account =
        this.accountRepository
            .findById(command.accountId())
            .orElseThrow(
                () ->
                    new ClientCreationException(
                        "ACCOUNT_NOT_FOUND", "The selected account does not exist."));

    String companyName = this.requireCompanyName(command.companyName());

    Client client =
        new Client(
            null,
            account.getId(),
            companyName,
            this.resolveStatus(command.status()),
            this.normalizeNotes(command.notes()),
            Instant.now(),
            null);

    return this.clientRepository.save(client);
  }

  private String requireCompanyName(String companyName) {
    if (companyName == null || companyName.trim().isEmpty()) {
      throw new IllegalArgumentException("companyName must not be blank");
    }

    return companyName.trim();
  }

  private String resolveStatus(String status) {
    if (status == null || status.trim().isBlank()) {
      return "prospect";
    }

    String normalized = status.trim().toLowerCase();
    switch (normalized) {
      case "prospect", "active", "inactive", "archived" -> {
        return normalized;
      }
      default -> throw new IllegalArgumentException("status must be one of: prospect, active, inactive, archived");
    }
  }

  private String normalizeNotes(String notes) {
    if (notes == null) {
      return null;
    }

    String trimmed = notes.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
