package com.antoineromand.atlascrm.account.application.usecase.account;

import com.antoineromand.atlascrm.account.application.exceptions.AccountNotFoundException;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetAccountUseCase implements IGetAccountUseCase {

  private final IAccountRepository accountRepository;

  public GetAccountUseCase(IAccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Account execute(UUID credentialsId) {
    return this.accountRepository.findByCredentialsId(credentialsId)
        .orElseThrow(AccountNotFoundException::new);
  }
}
