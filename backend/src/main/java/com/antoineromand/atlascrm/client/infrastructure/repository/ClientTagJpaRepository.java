package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.infrastructure.model.ClientTagEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientTagJpaRepository extends JpaRepository<ClientTagEntity, UUID> {
  List<ClientTagEntity> findAllByAccount_IdOrderByNameAsc(UUID accountId);
}
