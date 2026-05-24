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
            this.resolve(command.firstName(), existing.getFirstName()),
            this.resolve(command.lastName(), existing.getLastName()),
            this.resolve(command.companyName(), existing.getCompanyName()),
            this.resolve(command.siretNumber(), existing.getSiretNumber()),
            this.resolve(command.vatNumber(), existing.getVatNumber()),
            this.resolve(command.billingEmail(), existing.getBillingEmail()),
            this.resolve(command.billingAddressLine1(), existing.getBillingAddressLine1()),
            this.resolve(command.billingAddressLine2(), existing.getBillingAddressLine2()),
            this.resolve(command.billingPostalCode(), existing.getBillingPostalCode()),
            this.resolve(command.billingCity(), existing.getBillingCity()),
            this.resolve(command.billingCountry(), existing.getBillingCountry()),
            existing.getCreatedAt(),
            existing.getUpdatedAt());

    this.accountRepository.save(updated);
    return this.accountRepository.findByCredentialsId(credentialsId)
        .orElseThrow(AccountNotFoundException::new);
  }

  private String resolve(PatchValue<String> patchValue, String currentValue) {
    if (patchValue == null || !patchValue.present()) {
      return currentValue;
    }
    return patchValue.value();
  }
}
