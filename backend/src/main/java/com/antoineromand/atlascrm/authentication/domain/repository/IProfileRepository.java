package com.antoineromand.atlascrm.authentication.domain.repository;

import com.antoineromand.atlascrm.authentication.domain.Profile;
import java.util.Optional;
import java.util.UUID;

public interface IProfileRepository {
  UUID save(Profile profile);

  Optional<Profile> findByCredentialsId(UUID credentialsId);
}
