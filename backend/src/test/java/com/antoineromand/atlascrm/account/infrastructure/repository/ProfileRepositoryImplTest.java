package com.antoineromand.atlascrm.account.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.account.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import com.antoineromand.atlascrm.authentication.infrastructure.database.AbstractPostgresJpaTest;
import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import com.antoineromand.atlascrm.authentication.infrastructure.repository.CredentialsJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(ProfileRepositoryImpl.class)
class ProfileRepositoryImplTest extends AbstractPostgresJpaTest {

  @Autowired private ProfileRepositoryImpl profileRepository;
  @Autowired private CredentialsJpaRepository credentialsJpaRepository;

  @Test
  void saveShouldPersistAndFindByCredentialsIdShouldMapToDomain() {
    CredentialsEntity credentialsEntity =
        credentialsJpaRepository.save(
            new CredentialsEntity(
                null,
                "freelance@example.com",
                "hashed-password",
                Instant.now(),
                null,
                RoleName.USER,
                CredentialsStatus.ACTIVE,
                true));

    Profile profile =
        new Profile(
            null,
            credentialsEntity.getId(),
            "John",
            "Doe",
            "JD Consulting",
            "12345678901234",
            "FR12345678901",
            "billing@example.com",
            "10 rue de Paris",
            null,
            "75000",
            "Paris",
            "France",
            Instant.now(),
            null);

    UUID profileId = profileRepository.save(profile);

    Profile found = profileRepository.findByCredentialsId(credentialsEntity.getId()).orElseThrow();

    assertEquals(profileId, found.getId());
    assertEquals(credentialsEntity.getId(), found.getCredentialsId());
    assertEquals("John", found.getFirstName());
    assertEquals("Doe", found.getLastName());
    assertEquals("JD Consulting", found.getCompanyName());
    assertEquals("12345678901234", found.getSiretNumber());
    assertTrue(found.getBillingEmail().contains("@"));
  }
}
