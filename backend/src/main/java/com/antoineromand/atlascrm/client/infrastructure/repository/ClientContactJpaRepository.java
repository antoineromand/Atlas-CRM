package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.infrastructure.model.ClientContactEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientContactJpaRepository extends JpaRepository<ClientContactEntity, UUID> {
  Optional<ClientContactEntity> findByClient_IdAndPrimaryTrue(UUID clientId);

  List<ClientContactEntity> findAllByClient_IdOrderByPrimaryDescCreatedAtAsc(UUID clientId);
}
