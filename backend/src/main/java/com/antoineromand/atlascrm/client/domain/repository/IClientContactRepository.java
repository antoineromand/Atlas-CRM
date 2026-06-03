package com.antoineromand.atlascrm.client.domain.repository;

import com.antoineromand.atlascrm.client.domain.ClientContact;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IClientContactRepository {
  UUID save(ClientContact contact);

  Optional<ClientContact> findById(UUID contactId);

  Optional<ClientContact> findPrimaryByClientId(UUID clientId);

  List<ClientContact> findAllByClientId(UUID clientId);

  void deleteById(UUID contactId);
}
