package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.account.infrastructure.repository.AccountJpaRepository;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    ClientEntity entity =
        client.getId() == null
            ? ClientEntity.fromDomain(client, account)
            : this.clientJpaRepository
                .findByIdAndAccount_Id(client.getId(), account.getId())
                .map(
                    existing -> {
                      existing.updateFields(client.getCompanyName(), client.getStatus(), client.getNotes());
                      return existing;
                    })
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

    ClientEntity saved = this.clientJpaRepository.save(entity);
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
  public Page<Client> findAllByAccountId(UUID accountId, Pageable pageable) {
    return this.clientJpaRepository.findAllByAccount_Id(accountId, pageable).map(ClientEntity::toDomain);
  }

  @Override
  public Page<Client> findAllByAccountIdAndSearch(
      UUID accountId, String search, String status, Pageable pageable) {
    return this.clientJpaRepository.findAllByAccountIdAndSearch(accountId, search, status, pageable)
        .map(ClientEntity::toDomain);
  }

  @Override
  public void deleteById(UUID clientId) {
    this.clientJpaRepository.deleteById(clientId);
  }
}
