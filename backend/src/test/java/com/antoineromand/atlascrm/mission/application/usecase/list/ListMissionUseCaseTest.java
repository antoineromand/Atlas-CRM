package com.antoineromand.atlascrm.mission.application.usecase.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

  @Test
  void executeShouldFilterMissionsWhenSearchIsProvided() {
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

    when(missionRepository.findAllByAccountIdAndSearch(accountId, "website")).thenReturn(missions);

    List<Mission> result = useCase.execute(accountId, " website ");

    assertEquals(1, result.size());
    assertEquals("Website redesign", result.get(0).getTitle());
    verify(missionRepository).findAllByAccountIdAndSearch(accountId, "website");
  }

  @Test
  void executeShouldReturnPagedResults() {
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

    when(missionRepository.findAllByAccountId(
            accountId, PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"))))
        .thenReturn(new PageImpl<>(missions, PageRequest.of(0, 6), 1));

    MissionPageResult result = useCase.execute(accountId, null, 1, 6);

    assertEquals(1, result.items().size());
    assertEquals(1, result.page());
    assertEquals(6, result.size());
    assertEquals(1L, result.totalElements());
    assertEquals(1, result.totalPages());
    assertEquals(false, result.hasNext());
    assertEquals(false, result.hasPrevious());
  }

  @Test
  void executeShouldReturnPagedSearchResults() {
    ListMissionUseCase useCase = new ListMissionUseCase(missionRepository);
    UUID accountId = UUID.randomUUID();
    List<Mission> missions =
        List.of(
            new Mission(
                UUID.randomUUID(),
                accountId,
                "Website redesign",
                "Frontend consultant",
                null,
                "in_progress",
                "high",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                Instant.now(),
                null));

    when(missionRepository.findAllByAccountIdAndSearch(
            accountId, "website", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))))
        .thenReturn(new PageImpl<>(missions, PageRequest.of(0, 10), 1));

    MissionPageResult result = useCase.execute(accountId, " website ", 1, 10);

    assertEquals(1, result.items().size());
    assertEquals("Website redesign", result.items().get(0).getTitle());
    verify(missionRepository)
        .findAllByAccountIdAndSearch(
            accountId, "website", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
  }
}
