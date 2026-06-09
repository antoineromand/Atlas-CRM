package com.antoineromand.atlascrm.mission.application.usecase.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.antoineromand.atlascrm.AtlascrmApplication;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import com.antoineromand.atlascrm.authentication.infrastructure.database.PostgresTestContainer;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = AtlascrmApplication.class)
class CreateMissionUseCaseIntegrationTest {

  @Autowired private CreateMissionUseCase createMissionUseCase;
  @Autowired private ICredentialsRepository credentialsRepository;
  @Autowired private IAccountRepository accountRepository;
  @Autowired private IMissionRepository missionRepository;

  @DynamicPropertySource
  static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", PostgresTestContainer.getInstance()::getJdbcUrl);
    registry.add("spring.datasource.username", PostgresTestContainer.getInstance()::getUsername);
    registry.add("spring.datasource.password", PostgresTestContainer.getInstance()::getPassword);
  }

  @Test
  void executeShouldPersistMissionEndToEnd() {
    String email = UUID.randomUUID() + "@example.com";
    UUID credentialsId =
        credentialsRepository.save(
            new Credentials(
                null,
                email,
                "hashed-password",
                RoleName.USER,
                Instant.now(),
                null,
                CredentialsStatus.ACTIVE,
                true));

    Account account =
        new Account(
            null,
            credentialsId,
            "John",
            "Doe",
            "JD Consulting",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Instant.now(),
            null);
    UUID accountId = accountRepository.save(account);

    UUID missionId =
        createMissionUseCase.execute(
            new CreateMissionCommand(
                accountId,
                null,
                "Website redesign",
                "Lead developer",
                "Redesign the marketing website",
                "in_progress",
                "high",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)));

    Mission mission = missionRepository.findById(missionId).orElseThrow();
    Optional<Account> persistedAccount = accountRepository.findById(accountId);

    assertEquals(accountId, mission.getAccountId());
    assertEquals("Website redesign", mission.getTitle());
    assertEquals("in_progress", mission.getStatus());
    assertEquals("high", mission.getPriority());
    assertNotNull(persistedAccount.orElseThrow());
  }
}
