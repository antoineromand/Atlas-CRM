package com.antoineromand.atlascrm.authentication.infrastructure.repository;

import com.antoineromand.atlascrm.authentication.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.repository.IProfileRepository;
import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import com.antoineromand.atlascrm.authentication.infrastructure.model.ProfileEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileRepositoryImpl implements IProfileRepository {

  private final ProfileJpaRepository profileJpaRepository;
  private final CredentialsJpaRepository credentialsJpaRepository;

  public ProfileRepositoryImpl(
      ProfileJpaRepository profileJpaRepository, CredentialsJpaRepository credentialsJpaRepository) {
    this.profileJpaRepository = profileJpaRepository;
    this.credentialsJpaRepository = credentialsJpaRepository;
  }

  @Override
  public UUID save(Profile profile) {
    CredentialsEntity credentials =
        this.credentialsJpaRepository
            .findById(profile.getCredentialsId())
            .orElseThrow(() -> new IllegalArgumentException("Credentials not found"));
    ProfileEntity saved = this.profileJpaRepository.save(ProfileEntity.fromDomain(profile, credentials));
    return saved.getId();
  }

  @Override
  public Optional<Profile> findByCredentialsId(UUID credentialsId) {
    return this.profileJpaRepository.findByCredentials_Id(credentialsId).map(ProfileEntity::toDomain);
  }
}
