package com.antoineromand.atlascrm.client.domain.repository;

import com.antoineromand.atlascrm.client.domain.ClientTag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IClientTagRepository {
  UUID save(ClientTag tag);

  Optional<ClientTag> findById(UUID tagId);

  List<ClientTag> findAllByAccountId(UUID accountId);

  List<ClientTag> findAllByClientId(UUID clientId);

  void deleteById(UUID tagId);
}
