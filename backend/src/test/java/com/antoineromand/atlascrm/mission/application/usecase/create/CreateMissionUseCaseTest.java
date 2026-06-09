package com.antoineromand.atlascrm.mission.application.usecase.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionCreationException;
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
class CreateMissionUseCaseTest {

  @Mock private IMissionRepository missionRepository;
  @Mock private IAccountRepository accountRepository;

  @Test
  void executeShouldPersistMissionAndApplyDefaultsWhenNeeded() {
    CreateMissionUseCase useCase = new CreateMissionUseCase(missionRepository, accountRepository);
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();

    when(accountRepository.findById(accountId))
        .thenReturn(
            Optional.of(
                new Account(
                    accountId,
                    UUID.randomUUID(),
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
                    Instant.now(),
                    null)));
    when(missionRepository.save(any(Mission.class))).thenReturn(missionId);

    UUID result =
        useCase.execute(
            new CreateMissionCommand(
                accountId,
                "Website redesign",
                "Lead developer",
                "Redesign the marketing website",
                null,
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)));

    ArgumentCaptor<Mission> captor = ArgumentCaptor.forClass(Mission.class);
    verify(missionRepository).save(captor.capture());
    Mission saved = captor.getValue();

    assertEquals(missionId, result);
    assertEquals(accountId, saved.getAccountId());
    assertEquals("Website redesign", saved.getTitle());
    assertEquals("Lead developer", saved.getRoleInProject());
    assertEquals("Redesign the marketing website", saved.getDescription());
    assertEquals(MissionStatus.CREATED, saved.getMissionStatus());
    assertEquals("created", saved.getStatus());
    assertEquals("medium", saved.getPriority());
    assertEquals(LocalDate.of(2026, 6, 1), saved.getStartDate());
    assertEquals(LocalDate.of(2026, 6, 30), saved.getDeadline());
  }

  @Test
  void executeShouldThrowWhenAccountDoesNotExist() {
    CreateMissionUseCase useCase = new CreateMissionUseCase(missionRepository, accountRepository);
    UUID accountId = UUID.randomUUID();

    when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

    MissionCreationException exception =
        assertThrows(
            MissionCreationException.class,
            () ->
                useCase.execute(
                    new CreateMissionCommand(
                        accountId,
                        "Website redesign",
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 6, 1),
                        null)));

    assertEquals("ACCOUNT_NOT_FOUND", exception.getCode());
    verify(missionRepository, never()).save(any());
  }
}
