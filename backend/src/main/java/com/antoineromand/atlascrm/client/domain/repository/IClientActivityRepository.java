package com.antoineromand.atlascrm.client.domain.repository;

import com.antoineromand.atlascrm.client.domain.ClientActivity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IClientActivityRepository {
  UUID save(ClientActivity activity);

  Optional<ClientActivity> findById(UUID activityId);

  List<ClientActivity> findAllByClientId(UUID clientId);

  void deleteById(UUID activityId);
}
