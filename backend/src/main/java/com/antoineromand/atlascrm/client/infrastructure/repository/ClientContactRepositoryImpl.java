package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.repository.IClientContactRepository;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientContactEntity;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ClientContactRepositoryImpl implements IClientContactRepository {

  private final ClientContactJpaRepository clientContactJpaRepository;
  private final ClientJpaRepository clientJpaRepository;

  public ClientContactRepositoryImpl(
      ClientContactJpaRepository clientContactJpaRepository, ClientJpaRepository clientJpaRepository) {
    this.clientContactJpaRepository = clientContactJpaRepository;
    this.clientJpaRepository = clientJpaRepository;
  }

  @Override
  public UUID save(ClientContact contact) {
    ClientEntity client =
        this.clientJpaRepository
            .findById(contact.getClientId())
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));

    if (contact.isPrimary()) {
      this.clientContactJpaRepository
          .findByClient_IdAndPrimaryTrue(contact.getClientId())
          .filter(existingPrimary -> !existingPrimary.getId().equals(contact.getId()))
          .ifPresent(
              existingPrimary -> {
                existingPrimary.setPrimary(false);
                this.clientContactJpaRepository.saveAndFlush(existingPrimary);
              });
    }

    ClientContactEntity saved =
        this.clientContactJpaRepository.save(ClientContactEntity.fromDomain(contact, client));
    return saved.getId();
  }

  @Override
  public Optional<ClientContact> findById(UUID contactId) {
    return this.clientContactJpaRepository.findById(contactId).map(ClientContactEntity::toDomain);
  }

  @Override
  public Optional<ClientContact> findPrimaryByClientId(UUID clientId) {
    return this.clientContactJpaRepository.findByClient_IdAndPrimaryTrue(clientId).map(ClientContactEntity::toDomain);
  }

  @Override
  public List<ClientContact> findAllByClientId(UUID clientId) {
    return this.clientContactJpaRepository.findAllByClient_IdOrderByPrimaryDescCreatedAtAsc(clientId)
        .stream()
        .map(ClientContactEntity::toDomain)
        .toList();
  }

  @Override
  public void deleteById(UUID contactId) {
    this.clientContactJpaRepository.deleteById(contactId);
  }
}
