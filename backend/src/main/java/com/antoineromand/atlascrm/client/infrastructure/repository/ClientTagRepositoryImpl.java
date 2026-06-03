package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.account.infrastructure.repository.AccountJpaRepository;
import com.antoineromand.atlascrm.client.domain.ClientTag;
import com.antoineromand.atlascrm.client.domain.repository.IClientTagRepository;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientTagEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ClientTagRepositoryImpl implements IClientTagRepository {

  private final ClientTagJpaRepository clientTagJpaRepository;
  private final AccountJpaRepository accountJpaRepository;

  public ClientTagRepositoryImpl(
      ClientTagJpaRepository clientTagJpaRepository, AccountJpaRepository accountJpaRepository) {
    this.clientTagJpaRepository = clientTagJpaRepository;
    this.accountJpaRepository = accountJpaRepository;
  }

  @Override
  public UUID save(ClientTag tag) {
    AccountEntity account =
        this.accountJpaRepository
            .findById(tag.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    ClientTagEntity saved = this.clientTagJpaRepository.save(ClientTagEntity.fromDomain(tag, account));
    return saved.getId();
  }

  @Override
  public Optional<ClientTag> findById(UUID tagId) {
    return this.clientTagJpaRepository.findById(tagId).map(ClientTagEntity::toDomain);
  }

  @Override
  public List<ClientTag> findAllByAccountId(UUID accountId) {
    return this.clientTagJpaRepository.findAllByAccount_IdOrderByNameAsc(accountId).stream()
        .map(ClientTagEntity::toDomain)
        .toList();
  }

  @Override
  public void deleteById(UUID tagId) {
    this.clientTagJpaRepository.deleteById(tagId);
  }
}
