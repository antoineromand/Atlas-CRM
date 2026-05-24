package com.antoineromand.atlascrm.authentication.infrastructure.repository;

import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class CredentialsRepositoryImpl implements ICredentialsRepository {

  private final CredentialsJpaRepository credentialsJpaRepository;

  public CredentialsRepositoryImpl(CredentialsJpaRepository credentialsJpaRepository) {
    this.credentialsJpaRepository = credentialsJpaRepository;
  }

  @Override
  public Optional<Credentials> findByEmail(String email) {
    return this.credentialsJpaRepository.findByEmail(email).map(CredentialsEntity::toDomain);
  }

  @Override
  public Optional<Credentials> findByUserId(String userId) {
    return this.credentialsJpaRepository.findById(UUID.fromString(userId)).map(CredentialsEntity::toDomain);
  }

  @Override
  public UUID save(Credentials credentials) {
    CredentialsEntity saved =
        this.credentialsJpaRepository.save(CredentialsEntity.fromDomain(credentials));
    return saved.getId();
  }
}
