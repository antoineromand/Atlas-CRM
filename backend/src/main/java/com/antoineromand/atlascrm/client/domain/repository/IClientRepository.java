package com.antoineromand.atlascrm.client.domain.repository;

import com.antoineromand.atlascrm.client.domain.Client;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IClientRepository {
  UUID save(Client client);

  Optional<Client> findById(UUID clientId);

  Optional<Client> findByIdAndAccountId(UUID clientId, UUID accountId);

  List<Client> findAllByAccountId(UUID accountId);

  void deleteById(UUID clientId);
}
