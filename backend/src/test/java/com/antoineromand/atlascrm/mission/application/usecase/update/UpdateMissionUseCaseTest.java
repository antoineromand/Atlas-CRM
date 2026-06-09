package com.antoineromand.atlascrm.mission.application.usecase.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionNotFoundException;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.MissionStatus;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateMissionUseCaseTest {

  @Mock private IMissionRepository missionRepository;

  @Test
  void executeShouldMergeFieldsAndReturnUpdatedMission() {
    UpdateMissionUseCase useCase = new UpdateMissionUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Mission existing =
        new Mission(
            missionId,
            accountId,
            "Initial title",
            "Lead developer",
            "Initial description",
            "created",
            "medium",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            createdAt,
            null);
    Mission updated =
        new Mission(
            missionId,
            accountId,
            "Updated title",
            "Lead developer",
            "Updated description",
            "in_progress",
            "high",
            LocalDate.of(2026, 6, 2),
            LocalDate.of(2026, 7, 1),
            createdAt,
            Instant.parse("2026-06-01T11:00:00Z"));

    when(missionRepository.findByIdAndAccountId(missionId, accountId))
        .thenReturn(Optional.of(existing), Optional.of(updated));
    when(missionRepository.save(any(Mission.class))).thenReturn(missionId);

    Mission result =
        useCase.execute(
            accountId,
            missionId,
            new UpdateMissionCommand(
                PatchValue.of("Updated title"),
                PatchValue.absent(),
                PatchValue.of("Updated description"),
                PatchValue.of("in_progress"),
                PatchValue.of("high"),
                PatchValue.of(LocalDate.of(2026, 6, 2)),
                PatchValue.of(LocalDate.of(2026, 7, 1))));

    ArgumentCaptor<Mission> captor = ArgumentCaptor.forClass(Mission.class);
    verify(missionRepository).save(captor.capture());
    Mission saved = captor.getValue();

    assertEquals(missionId, result.getId());
    assertEquals("Updated title", saved.getTitle());
    assertEquals("Lead developer", saved.getRoleInProject());
    assertEquals("Updated description", saved.getDescription());
    assertEquals(MissionStatus.IN_PROGRESS, saved.getMissionStatus());
    assertEquals("in_progress", saved.getStatus());
    assertEquals("high", saved.getPriority());
    assertEquals(LocalDate.of(2026, 6, 2), saved.getStartDate());
    assertEquals(LocalDate.of(2026, 7, 1), saved.getDeadline());
  }

  @Test
  void executeShouldThrowWhenMissionDoesNotExistForAccount() {
    UpdateMissionUseCase useCase = new UpdateMissionUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();

    when(missionRepository.findByIdAndAccountId(missionId, accountId)).thenReturn(Optional.empty());

    assertThrows(
        MissionNotFoundException.class,
        () ->
            useCase.execute(
                accountId,
                missionId,
                new UpdateMissionCommand(
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent())));
  }
}
