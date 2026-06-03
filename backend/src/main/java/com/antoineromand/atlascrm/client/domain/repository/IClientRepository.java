package com.antoineromand.atlascrm.client.domain.repository;

import com.antoineromand.atlascrm.client.domain.Client;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IClientRepository {
  UUID save(Client client);

  Optional<Client> findById(UUID clientId);

  Optional<Client> findByIdAndAccountId(UUID clientId, UUID accountId);

  List<Client> findAllByAccountId(UUID accountId);

  List<Client> findAllByAccountIdAndSearch(UUID accountId, String search, String status);

  Page<Client> findAllByAccountId(UUID accountId, Pageable pageable);

  Page<Client> findAllByAccountIdAndSearch(UUID accountId, String search, String status, Pageable pageable);

  void deleteById(UUID clientId);
}
