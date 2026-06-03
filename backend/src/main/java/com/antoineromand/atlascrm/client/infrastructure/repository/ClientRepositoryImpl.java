package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.account.infrastructure.repository.AccountJpaRepository;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ClientRepositoryImpl implements IClientRepository {

  private final ClientJpaRepository clientJpaRepository;
  private final AccountJpaRepository accountJpaRepository;

  public ClientRepositoryImpl(
      ClientJpaRepository clientJpaRepository, AccountJpaRepository accountJpaRepository) {
    this.clientJpaRepository = clientJpaRepository;
    this.accountJpaRepository = accountJpaRepository;
  }

  @Override
  public UUID save(Client client) {
    AccountEntity account =
        this.accountJpaRepository
            .findById(client.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    ClientEntity saved = this.clientJpaRepository.save(ClientEntity.fromDomain(client, account));
    return saved.getId();
  }

  @Override
  public Optional<Client> findById(UUID clientId) {
    return this.clientJpaRepository.findById(clientId).map(ClientEntity::toDomain);
  }

  @Override
  public Optional<Client> findByIdAndAccountId(UUID clientId, UUID accountId) {
    return this.clientJpaRepository.findByIdAndAccount_Id(clientId, accountId).map(ClientEntity::toDomain);
  }

  @Override
  public List<Client> findAllByAccountId(UUID accountId) {
    return this.clientJpaRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId).stream()
        .map(ClientEntity::toDomain)
        .toList();
  }

  @Override
  public void deleteById(UUID clientId) {
    this.clientJpaRepository.deleteById(clientId);
  }
}
