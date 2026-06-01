package com.antoineromand.atlascrm.mission.application.usecase.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
class GetMissionUseCaseTest {

  @Mock private IMissionRepository missionRepository;

  @Test
  void executeShouldReturnMissionWhenOwnedByAccount() {
    GetMissionUseCase useCase = new GetMissionUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();
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
            Instant.now(),
            null);

    when(missionRepository.findByIdAndAccountId(missionId, accountId)).thenReturn(Optional.of(mission));

    Mission result = useCase.execute(accountId, missionId);

    assertEquals(missionId, result.getId());
    assertEquals(accountId, result.getAccountId());
  }

  @Test
  void executeShouldThrowWhenMissionDoesNotExistForAccount() {
    GetMissionUseCase useCase = new GetMissionUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();

    when(missionRepository.findByIdAndAccountId(missionId, accountId)).thenReturn(Optional.empty());

    assertThrows(MissionNotFoundException.class, () -> useCase.execute(accountId, missionId));
  }
}
