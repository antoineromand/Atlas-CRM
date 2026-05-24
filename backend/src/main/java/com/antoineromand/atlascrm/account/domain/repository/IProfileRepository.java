package com.antoineromand.atlascrm.account.domain.repository;

import com.antoineromand.atlascrm.account.domain.Profile;
import java.util.Optional;
import java.util.UUID;

public interface IProfileRepository {
  UUID save(Profile profile);

  Optional<Profile> findByCredentialsId(UUID credentialsId);
}
