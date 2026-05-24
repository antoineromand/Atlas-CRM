package com.antoineromand.atlascrm.account.domain.repository;

import com.antoineromand.atlascrm.account.domain.Account;
import java.util.Optional;
import java.util.UUID;

public interface IAccountRepository {
  UUID save(Account account);

  Optional<Account> findByCredentialsId(UUID credentialsId);
}
