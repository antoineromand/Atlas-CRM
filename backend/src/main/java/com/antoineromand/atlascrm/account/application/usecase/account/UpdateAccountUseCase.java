package com.antoineromand.atlascrm.account.application.usecase.account;

import com.antoineromand.atlascrm.account.application.exceptions.AccountNotFoundException;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateAccountUseCase implements IUpdateAccountUseCase {

  private final IAccountRepository accountRepository;

  public UpdateAccountUseCase(IAccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Account execute(UUID credentialsId, UpdateAccountCommand command) {
    Account existing =
        this.accountRepository.findByCredentialsId(credentialsId)
            .orElseThrow(AccountNotFoundException::new);

    Account updated =
        new Account(
            existing.getId(),
            existing.getCredentialsId(),
            command.firstName() != null ? command.firstName() : existing.getFirstName(),
            command.lastName() != null ? command.lastName() : existing.getLastName(),
            command.companyName() != null ? command.companyName() : existing.getCompanyName(),
            command.siretNumber() != null ? command.siretNumber() : existing.getSiretNumber(),
            command.vatNumber() != null ? command.vatNumber() : existing.getVatNumber(),
            command.billingEmail() != null ? command.billingEmail() : existing.getBillingEmail(),
            command.billingAddressLine1() != null
                ? command.billingAddressLine1()
                : existing.getBillingAddressLine1(),
            command.billingAddressLine2() != null
                ? command.billingAddressLine2()
                : existing.getBillingAddressLine2(),
            command.billingPostalCode() != null
                ? command.billingPostalCode()
                : existing.getBillingPostalCode(),
            command.billingCity() != null ? command.billingCity() : existing.getBillingCity(),
            command.billingCountry() != null ? command.billingCountry() : existing.getBillingCountry(),
            existing.getCreatedAt(),
            existing.getUpdatedAt());

    this.accountRepository.save(updated);
    return this.accountRepository.findByCredentialsId(credentialsId)
        .orElseThrow(AccountNotFoundException::new);
  }
}
