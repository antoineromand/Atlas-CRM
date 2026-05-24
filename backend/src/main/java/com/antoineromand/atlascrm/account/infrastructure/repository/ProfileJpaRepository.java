package com.antoineromand.atlascrm.account.infrastructure.repository;

import com.antoineromand.atlascrm.account.infrastructure.model.ProfileEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileJpaRepository extends JpaRepository<ProfileEntity, UUID> {
  Optional<ProfileEntity> findByCredentials_Id(UUID credentialsId);
}
