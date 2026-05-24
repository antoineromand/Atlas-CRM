package com.antoineromand.atlascrm.authentication.domain.repository;

import com.antoineromand.atlascrm.authentication.domain.Credentials;
import java.util.Optional;
import java.util.UUID;

public interface ICredentialsRepository {
  Optional<Credentials> findByEmail(String email);

  Optional<Credentials> findByUserId(String userId);

  UUID save(Credentials credentials);
}
