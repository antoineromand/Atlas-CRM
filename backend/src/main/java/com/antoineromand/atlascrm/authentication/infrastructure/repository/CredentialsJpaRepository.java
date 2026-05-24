package com.antoineromand.atlascrm.authentication.infrastructure.repository;

import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialsJpaRepository extends JpaRepository<CredentialsEntity, UUID> {
  Optional<CredentialsEntity> findByEmail(String email);
}
