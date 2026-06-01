package com.antoineromand.atlascrm.mission.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.account.infrastructure.repository.AccountJpaRepository;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import com.antoineromand.atlascrm.authentication.infrastructure.database.AbstractPostgresJpaTest;
import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import com.antoineromand.atlascrm.authentication.infrastructure.repository.CredentialsJpaRepository;
import com.antoineromand.atlascrm.mission.domain.Mission;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(MissionRepositoryImpl.class)
class MissionRepositoryImplTest extends AbstractPostgresJpaTest {

  @Autowired private MissionRepositoryImpl missionRepository;
  @Autowired private MissionJpaRepository missionJpaRepository;
  @Autowired private AccountJpaRepository accountJpaRepository;
  @Autowired private CredentialsJpaRepository credentialsJpaRepository;

  @Test
  void saveShouldPersistAndFindMethodsShouldMapToDomain() {
    AccountEntity account = persistAccount();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");

    Mission mission =
        new Mission(
            null,
            account.getId(),
            "Website redesign",
            "Lead developer",
            "Redesign the marketing website",
            "in_progress",
            "high",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            createdAt,
            null);

    UUID missionId = missionRepository.save(mission);

    Mission found = missionRepository.findById(missionId).orElseThrow();
    List<Mission> missions = missionRepository.findAllByAccountId(account.getId());

    assertEquals(missionId, found.getId());
    assertEquals(account.getId(), found.getAccountId());
    assertEquals("Website redesign", found.getTitle());
    assertEquals("Lead developer", found.getRoleInProject());
    assertEquals("in_progress", found.getStatus());
    assertEquals("high", found.getPriority());
    assertEquals(LocalDate.of(2026, 6, 1), found.getStartDate());
    assertEquals(LocalDate.of(2026, 6, 30), found.getDeadline());
    assertEquals(1, missions.size());
    assertEquals(missionId, missions.get(0).getId());
  }

  @Test
  void deleteByIdShouldRemovePersistedMission() {
    AccountEntity account = persistAccount();
    Mission mission =
        new Mission(
            null,
            account.getId(),
            "Mobile app",
            null,
            null,
            "not_started",
            "medium",
            LocalDate.of(2026, 6, 1),
            null,
            Instant.parse("2026-06-01T10:00:00Z"),
            null);

    UUID missionId = missionRepository.save(mission);

    assertTrue(missionRepository.findById(missionId).isPresent());

    missionRepository.deleteById(missionId);

    assertFalse(missionRepository.findById(missionId).isPresent());
  }

  private AccountEntity persistAccount() {
    CredentialsEntity credentials =
        credentialsJpaRepository.save(
            new CredentialsEntity(
                null,
                "freelancer@example.com",
                "hashed-password",
                Instant.now(),
                null,
                RoleName.USER,
                CredentialsStatus.ACTIVE,
                true));

    return accountJpaRepository.save(
        new AccountEntity(
            null,
            credentials,
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
            null));
  }
}
