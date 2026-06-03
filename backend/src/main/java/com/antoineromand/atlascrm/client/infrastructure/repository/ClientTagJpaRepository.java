package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.infrastructure.model.ClientTagEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientTagJpaRepository extends JpaRepository<ClientTagEntity, UUID> {
  List<ClientTagEntity> findAllByAccount_IdOrderByNameAsc(UUID accountId);

  @Query(
      """
      select distinct t
      from ClientTagEntity t
      join t.clients c
      where c.id = :clientId
      order by t.name asc
      """)
  List<ClientTagEntity> findAllByClientId(@Param("clientId") UUID clientId);
}
