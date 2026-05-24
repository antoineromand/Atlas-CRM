package com.antoineromand.atlascrm.account.infrastructure.repository;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
  Optional<AccountEntity> findByCredentials_Id(UUID credentialsId);
}
