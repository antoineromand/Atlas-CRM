package com.antoineromand.atlascrm.authentication.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import com.antoineromand.atlascrm.authentication.infrastructure.database.AbstractPostgresJpaTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(CredentialsRepositoryImpl.class)
class CredentialsRepositoryImplTest extends AbstractPostgresJpaTest {

  @Autowired private CredentialsRepositoryImpl credentialsRepository;

  @Test
  void saveShouldPersistAndFindByEmailShouldMapToDomain() {
    Credentials credentials =
        new Credentials(
            null,
            "user@example.com",
            "hashed-password",
            RoleName.USER,
            Instant.now(),
            null,
            CredentialsStatus.ACTIVE,
            true);

    UUID id = credentialsRepository.save(credentials);

    Credentials found = credentialsRepository.findByEmail("user@example.com").orElseThrow();

    assertEquals(id, found.getId());
    assertEquals("user@example.com", found.getEmail());
    assertEquals(RoleName.USER, found.getRole());
    assertEquals(CredentialsStatus.ACTIVE, found.getStatus());
    assertTrue(found.isEmailVerified());
  }

  @Test
  void findByUserIdShouldReturnPersistedCredentials() {
    Credentials credentials =
        new Credentials(
            null,
            "user2@example.com",
            "hashed-password",
            RoleName.USER,
            Instant.now(),
            null,
            CredentialsStatus.ACTIVE,
            false);

    UUID id = credentialsRepository.save(credentials);

    Credentials found = credentialsRepository.findByUserId(id.toString()).orElseThrow();

    assertEquals(id, found.getId());
    assertEquals("user2@example.com", found.getEmail());
  }
}
