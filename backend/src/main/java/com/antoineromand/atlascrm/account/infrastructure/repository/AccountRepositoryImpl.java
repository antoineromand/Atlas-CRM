package com.antoineromand.atlascrm.account.infrastructure.repository;

import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import com.antoineromand.atlascrm.authentication.infrastructure.repository.CredentialsJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepositoryImpl implements IAccountRepository {

  private final AccountJpaRepository accountJpaRepository;
  private final CredentialsJpaRepository credentialsJpaRepository;

  public AccountRepositoryImpl(
      AccountJpaRepository accountJpaRepository, CredentialsJpaRepository credentialsJpaRepository) {
    this.accountJpaRepository = accountJpaRepository;
    this.credentialsJpaRepository = credentialsJpaRepository;
  }

  @Override
  public UUID save(Account account) {
    CredentialsEntity credentials =
        this.credentialsJpaRepository
            .findById(account.getCredentialsId())
            .orElseThrow(() -> new IllegalArgumentException("Credentials not found"));
    AccountEntity saved = this.accountJpaRepository.save(AccountEntity.fromDomain(account, credentials));
    return saved.getId();
  }

  @Override
  public Optional<Account> findById(UUID accountId) {
    return this.accountJpaRepository.findById(accountId).map(AccountEntity::toDomain);
  }

  @Override
  public Optional<Account> findByCredentialsId(UUID credentialsId) {
    return this.accountJpaRepository.findByCredentials_Id(credentialsId).map(AccountEntity::toDomain);
  }
}
