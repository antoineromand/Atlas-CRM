package com.antoineromand.atlascrm.mission.application.usecase.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.mission.application.exceptions.MissionUpdateException;
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
class UpdateMissionStatusUseCaseTest {

  @Mock private IMissionRepository missionRepository;

  @Test
  void executeShouldAdvanceStatusByOneStep() {
    UpdateMissionStatusUseCase useCase = new UpdateMissionStatusUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Mission existing =
        new Mission(
            missionId,
            accountId,
            null,
            "Initial title",
            "Lead developer",
            "Initial description",
            MissionStatus.CREATED,
            "medium",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            createdAt,
            null);
    Mission updated =
        new Mission(
            missionId,
            accountId,
            null,
            "Initial title",
            "Lead developer",
            "Initial description",
            MissionStatus.ANALYSED,
            "medium",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            createdAt,
            Instant.parse("2026-06-01T11:00:00Z"));

    when(missionRepository.findByIdAndAccountId(missionId, accountId))
        .thenReturn(Optional.of(existing), Optional.of(updated));
    when(missionRepository.save(any(Mission.class))).thenReturn(missionId);

    Mission result =
        useCase.execute(new UpdateMissionStatusCommand(accountId, missionId, "analysed"));

    ArgumentCaptor<Mission> captor = ArgumentCaptor.forClass(Mission.class);
    verify(missionRepository).save(captor.capture());
    Mission saved = captor.getValue();

    assertEquals(missionId, result.getId());
    assertEquals(MissionStatus.ANALYSED, saved.getMissionStatus());
    assertEquals("analysed", saved.getStatus());
  }

  @Test
  void executeShouldRejectSkippedTransition() {
    UpdateMissionStatusUseCase useCase = new UpdateMissionStatusUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Mission existing =
        new Mission(
            missionId,
            accountId,
            null,
            "Initial title",
            "Lead developer",
            "Initial description",
            MissionStatus.CREATED,
            "medium",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            createdAt,
            null);

    when(missionRepository.findByIdAndAccountId(missionId, accountId)).thenReturn(Optional.of(existing));

    MissionUpdateException exception =
        assertThrows(
            MissionUpdateException.class,
            () -> useCase.execute(new UpdateMissionStatusCommand(accountId, missionId, "started")));

    assertEquals("MISSION_STATUS_INVALID_TRANSITION", exception.getCode());
    verify(missionRepository, never()).save(any());
  }
}
