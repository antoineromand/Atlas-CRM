package com.antoineromand.atlascrm.mission.application.usecase.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.mission.application.exceptions.MissionNotFoundException;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteMissionUseCaseTest {

  @Mock private IMissionRepository missionRepository;

  @Test
  void executeShouldDeleteMissionOwnedByAccount() {
    DeleteMissionUseCase useCase = new DeleteMissionUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();

    when(missionRepository.findByIdAndAccountId(missionId, accountId))
        .thenReturn(
            Optional.of(
                new Mission(
                    missionId,
                    accountId,
                    "Website redesign",
                    null,
                    null,
                    "in_progress",
                    "high",
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30),
                    Instant.now(),
                    null)));

    useCase.execute(accountId, missionId);

    verify(missionRepository).deleteById(missionId);
  }

  @Test
  void executeShouldThrowWhenMissionDoesNotExistForAccount() {
    DeleteMissionUseCase useCase = new DeleteMissionUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();

    when(missionRepository.findByIdAndAccountId(missionId, accountId)).thenReturn(Optional.empty());

    assertThrows(MissionNotFoundException.class, () -> useCase.execute(accountId, missionId));
  }
}
