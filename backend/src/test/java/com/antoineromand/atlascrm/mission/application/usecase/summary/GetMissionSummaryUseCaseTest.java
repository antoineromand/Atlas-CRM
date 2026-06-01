package com.antoineromand.atlascrm.mission.application.usecase.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMissionSummaryUseCaseTest {

  @Mock private IMissionRepository missionRepository;

  @Test
  void executeShouldReturnAggregatedMissionStats() {
    GetMissionSummaryUseCase useCase = new GetMissionSummaryUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    List<Mission> missions =
        List.of(
            new Mission(
                UUID.randomUUID(),
                accountId,
                "Website redesign",
                null,
                null,
                "in_progress",
                "high",
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(3),
                Instant.now(),
                null),
            new Mission(
                UUID.randomUUID(),
                accountId,
                "Invoice cleanup",
                null,
                null,
                "completed",
                "medium",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                Instant.now(),
                null));

    when(missionRepository.findAllByAccountId(accountId)).thenReturn(missions);

    MissionSummaryResult result = useCase.execute(accountId);

    assertEquals(2L, result.totalMissions());
    assertEquals(1L, result.activeMissions());
    assertEquals(1L, result.completedMissions());
    assertEquals(1L, result.dueSoonMissions());
    assertEquals(1L, result.highPriorityMissions());
  }
}
