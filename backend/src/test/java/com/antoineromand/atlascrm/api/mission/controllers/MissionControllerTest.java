package com.antoineromand.atlascrm.api.mission.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionResponseDto;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionRequestDto;
import com.antoineromand.atlascrm.mission.application.usecase.create.CreateMissionCommand;
import com.antoineromand.atlascrm.mission.application.usecase.create.ICreateMissionUseCase;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class MissionControllerTest {

  @Mock private ICreateMissionUseCase createMissionUseCase;
  @Mock private IGetAccountUseCase getAccountUseCase;

  @Test
  void createMissionShouldResolveCurrentAccountAndReturnCreatedResponse() {
    MissionController controller = new MissionController(createMissionUseCase, getAccountUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();
    Principal principal = () -> credentialsId.toString();

    when(getAccountUseCase.execute(credentialsId))
        .thenReturn(
            new Account(
                accountId,
                credentialsId,
                "John",
                "Doe",
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
                null));
    when(createMissionUseCase.execute(any(CreateMissionCommand.class))).thenReturn(missionId);

    ResponseEntity<CreateMissionResponseDto> response =
        controller.createMission(
            principal,
            new CreateMissionRequestDto(
                "Website redesign",
                "Lead developer",
                "Redesign the marketing website",
                "in_progress",
                "high",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)));

    ArgumentCaptor<CreateMissionCommand> captor =
        ArgumentCaptor.forClass(CreateMissionCommand.class);
    verify(createMissionUseCase).execute(captor.capture());

    CreateMissionCommand command = captor.getValue();
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(missionId, response.getBody().missionId());
    assertEquals("Mission created successfully.", response.getBody().message());
    assertEquals(accountId, command.accountId());
    assertEquals("Website redesign", command.title());
    assertEquals("Lead developer", command.roleInProject());
    assertEquals("Redesign the marketing website", command.description());
    assertEquals("in_progress", command.status());
    assertEquals("high", command.priority());
    assertEquals(LocalDate.of(2026, 6, 1), command.startDate());
    assertEquals(LocalDate.of(2026, 6, 30), command.deadline());
  }
}
