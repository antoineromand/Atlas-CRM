package com.antoineromand.atlascrm.account.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.antoineromand.atlascrm.account.domain.Account;
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

@Import(AccountRepositoryImpl.class)
class AccountRepositoryImplTest extends AbstractPostgresJpaTest {

  @Autowired private AccountRepositoryImpl accountRepository;
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

    Account account =
        new Account(
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

    UUID accountId = accountRepository.save(account);

    Account found = accountRepository.findByCredentialsId(credentialsEntity.getId()).orElseThrow();

    assertEquals(accountId, found.getId());
    assertEquals(credentialsEntity.getId(), found.getCredentialsId());
    assertEquals("John", found.getFirstName());
    assertEquals("Doe", found.getLastName());
    assertEquals("JD Consulting", found.getCompanyName());
    assertEquals("12345678901234", found.getSiretNumber());
    assertTrue(found.getBillingEmail().contains("@"));
  }
}
