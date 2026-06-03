package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.infrastructure.model.ClientActivityEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientActivityJpaRepository extends JpaRepository<ClientActivityEntity, UUID> {
  List<ClientActivityEntity> findAllByClient_IdOrderByOccurredAtDesc(UUID clientId);
}
