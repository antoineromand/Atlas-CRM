package com.antoineromand.atlascrm.mission.infrastructure.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.MissionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MissionEntityTest {

  @Test
  void fromDomainShouldMapAllFieldsToEntity() {
    UUID missionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Instant updatedAt = Instant.parse("2026-06-01T11:00:00Z");

    Mission mission =
        new Mission(
            missionId,
            accountId,
            "Website redesign",
            "Lead developer",
            "Redesign the marketing website",
            "in_progress",
            "high",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            createdAt,
            updatedAt);

    AccountEntity account = new AccountEntity();
    MissionEntity entity = MissionEntity.fromDomain(mission, account);

    assertEquals(missionId, entity.getId());
    assertEquals(account, entity.getAccount());
    assertEquals("Website redesign", entity.getTitle());
    assertEquals("Lead developer", entity.getRoleInProject());
    assertEquals("Redesign the marketing website", entity.getDescription());
    assertEquals("in_progress", entity.getStatus());
    assertEquals(60, mission.getProgress());
    assertEquals("high", entity.getPriority());
    assertEquals(LocalDate.of(2026, 6, 1), entity.getStartDate());
    assertEquals(LocalDate.of(2026, 6, 30), entity.getDeadline());
    assertEquals(createdAt, entity.getCreatedAt());
    assertEquals(updatedAt, entity.getUpdatedAt());
  }

  @Test
  void toDomainShouldMapAllFieldsToDomain() {
    UUID accountId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Instant updatedAt = Instant.parse("2026-06-01T11:00:00Z");

    CredentialsEntity credentials =
        new CredentialsEntity(
            UUID.randomUUID(),
            "freelancer@example.com",
            "hashed-password",
            createdAt,
            updatedAt,
            RoleName.USER,
            CredentialsStatus.ACTIVE,
            true);
    AccountEntity account =
        new AccountEntity(
            accountId,
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
            createdAt,
            updatedAt);

    MissionEntity entity =
        new MissionEntity(
            UUID.randomUUID(),
            account,
            "Website redesign",
            "Lead developer",
            "Redesign the marketing website",
            "in_progress",
            "high",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            createdAt,
            updatedAt);

    Mission mission = entity.toDomain();

    assertEquals(entity.getId(), mission.getId());
    assertEquals(accountId, mission.getAccountId());
    assertEquals("Website redesign", mission.getTitle());
    assertEquals("Lead developer", mission.getRoleInProject());
    assertEquals("Redesign the marketing website", mission.getDescription());
    assertEquals("in_progress", mission.getStatus());
    assertEquals(MissionStatus.IN_PROGRESS, mission.getMissionStatus());
    assertEquals("high", mission.getPriority());
    assertEquals(LocalDate.of(2026, 6, 1), mission.getStartDate());
    assertEquals(LocalDate.of(2026, 6, 30), mission.getDeadline());
    assertEquals(createdAt, mission.getCreatedAt());
    assertEquals(updatedAt, mission.getUpdatedAt());
  }

  @Test
  void toDomainShouldKeepOptionalFieldsNull() {
    CredentialsEntity credentials =
        new CredentialsEntity(
            UUID.randomUUID(),
            "freelancer@example.com",
            "hashed-password",
            null,
            null,
            RoleName.USER,
            CredentialsStatus.ACTIVE,
            true);
    AccountEntity account =
        new AccountEntity(
            UUID.randomUUID(),
            credentials,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    MissionEntity entity =
        new MissionEntity(
            null,
            account,
            "Website redesign",
            null,
            null,
            "created",
            "medium",
            LocalDate.of(2026, 6, 1),
            null,
            null,
            null);

    Mission mission = entity.toDomain();

    assertNull(mission.getRoleInProject());
    assertNull(mission.getDescription());
    assertNull(mission.getDeadline());
    assertNull(mission.getCreatedAt());
    assertNull(mission.getUpdatedAt());
    assertEquals("created", mission.getStatus());
  }
}
