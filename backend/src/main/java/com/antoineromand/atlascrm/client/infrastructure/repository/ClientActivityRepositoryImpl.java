package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.repository.IClientActivityRepository;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientActivityEntity;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ClientActivityRepositoryImpl implements IClientActivityRepository {

  private final ClientActivityJpaRepository clientActivityJpaRepository;
  private final ClientJpaRepository clientJpaRepository;

  public ClientActivityRepositoryImpl(
      ClientActivityJpaRepository clientActivityJpaRepository, ClientJpaRepository clientJpaRepository) {
    this.clientActivityJpaRepository = clientActivityJpaRepository;
    this.clientJpaRepository = clientJpaRepository;
  }

  @Override
  public UUID save(ClientActivity activity) {
    ClientEntity client =
        this.clientJpaRepository
            .findById(activity.getClientId())
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    ClientActivityEntity saved =
        this.clientActivityJpaRepository.save(ClientActivityEntity.fromDomain(activity, client));
    return saved.getId();
  }

  @Override
  public Optional<ClientActivity> findById(UUID activityId) {
    return this.clientActivityJpaRepository.findById(activityId).map(ClientActivityEntity::toDomain);
  }

  @Override
  public List<ClientActivity> findAllByClientId(UUID clientId) {
    return this.clientActivityJpaRepository.findAllByClient_IdOrderByOccurredAtDesc(clientId)
        .stream()
        .map(ClientActivityEntity::toDomain)
        .toList();
  }

  @Override
  public void deleteById(UUID activityId) {
    this.clientActivityJpaRepository.deleteById(activityId);
  }
}
