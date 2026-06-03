package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.infrastructure.model.ClientEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
  Optional<ClientEntity> findByIdAndAccount_Id(UUID clientId, UUID accountId);

  List<ClientEntity> findAllByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
