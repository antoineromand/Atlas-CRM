package com.antoineromand.atlascrm.mission.application.usecase.list;

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
class ListMissionUseCaseTest {

  @Mock private IMissionRepository missionRepository;

  @Test
  void executeShouldReturnAllMissionsForAccount() {
    ListMissionUseCase useCase = new ListMissionUseCase(missionRepository);
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
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                Instant.now(),
                null));

    when(missionRepository.findAllByAccountId(accountId)).thenReturn(missions);

    List<Mission> result = useCase.execute(accountId);

    assertEquals(1, result.size());
    assertEquals(accountId, result.get(0).getAccountId());
  }
}
