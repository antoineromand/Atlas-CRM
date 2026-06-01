package com.antoineromand.atlascrm.api.mission.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionResponseDto;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionRequestDto;
import com.antoineromand.atlascrm.api.mission.dto.MissionPageResponseDto;
import com.antoineromand.atlascrm.api.mission.dto.MissionResponseDto;
import com.antoineromand.atlascrm.mission.application.usecase.create.CreateMissionCommand;
import com.antoineromand.atlascrm.mission.application.usecase.create.ICreateMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.delete.IDeleteMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.get.IGetMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.list.IListMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.update.IUpdateMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.update.UpdateMissionCommand;
import com.antoineromand.atlascrm.mission.domain.Mission;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
  @Mock private IGetMissionUseCase getMissionUseCase;
  @Mock private IListMissionUseCase listMissionUseCase;
  @Mock private IUpdateMissionUseCase updateMissionUseCase;
  @Mock private IDeleteMissionUseCase deleteMissionUseCase;

  @Test
  void createMissionShouldResolveCurrentAccountAndReturnCreatedResponse() {
    MissionController controller =
        new MissionController(
            createMissionUseCase,
            getAccountUseCase,
            getMissionUseCase,
            listMissionUseCase,
            updateMissionUseCase,
            deleteMissionUseCase);
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

  @Test
  void listMyMissionsShouldReturnMissionDtos() {
    MissionController controller =
        new MissionController(
            createMissionUseCase,
            getAccountUseCase,
            getMissionUseCase,
            listMissionUseCase,
            updateMissionUseCase,
            deleteMissionUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
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
    when(listMissionUseCase.execute(accountId, null, 1, 6))
        .thenReturn(
            new com.antoineromand.atlascrm.mission.application.usecase.list.MissionPageResult(
                List.of(
                    new Mission(
                        UUID.randomUUID(),
                        accountId,
                        "Website redesign",
                        "Lead developer",
                        "Redesign the marketing website",
                        "in_progress",
                        "high",
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 30),
                        Instant.parse("2026-06-01T10:00:00Z"),
                        null)),
                1,
                6,
                1,
                1,
                false,
                false));

    ResponseEntity<MissionPageResponseDto> response = controller.listMyMissions(principal, null, 1, 6);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().items().size());
    assertEquals("Website redesign", response.getBody().items().get(0).title());
    assertEquals(1, response.getBody().page());
    assertEquals(6, response.getBody().size());
  }

  @Test
  void listMyMissionsShouldForwardSearchToUseCase() {
    MissionController controller =
        new MissionController(
            createMissionUseCase,
            getAccountUseCase,
            getMissionUseCase,
            listMissionUseCase,
            updateMissionUseCase,
            deleteMissionUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
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
    when(listMissionUseCase.execute(accountId, "website", 1, 6))
        .thenReturn(
            new com.antoineromand.atlascrm.mission.application.usecase.list.MissionPageResult(
                List.of(),
                1,
                6,
                0,
                0,
                false,
                false));

    ResponseEntity<MissionPageResponseDto> response =
        controller.listMyMissions(principal, "website", 1, 6);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().items().size());
  }

  @Test
  void listMyMissionsShouldRejectShortSearchQueries() {
    MissionController controller =
        new MissionController(
            createMissionUseCase,
            getAccountUseCase,
            getMissionUseCase,
            listMissionUseCase,
            updateMissionUseCase,
            deleteMissionUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
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

    assertThrows(IllegalArgumentException.class, () -> controller.listMyMissions(principal, "de", 1, 6));
  }

  @Test
  void getMyMissionShouldReturnMissionDto() {
    MissionController controller =
        new MissionController(
            createMissionUseCase,
            getAccountUseCase,
            getMissionUseCase,
            listMissionUseCase,
            updateMissionUseCase,
            deleteMissionUseCase);
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
    when(getMissionUseCase.execute(accountId, missionId))
        .thenReturn(
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
                Instant.parse("2026-06-01T10:00:00Z"),
                null));

    ResponseEntity<MissionResponseDto> response = controller.getMyMission(principal, missionId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(missionId, response.getBody().id());
    assertEquals("Website redesign", response.getBody().title());
  }

  @Test
  void updateMyMissionShouldResolvePatchAndReturnUpdatedResponse() {
    MissionController controller =
        new MissionController(
            createMissionUseCase,
            getAccountUseCase,
            getMissionUseCase,
            listMissionUseCase,
            updateMissionUseCase,
            deleteMissionUseCase);
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
    when(updateMissionUseCase.execute(any(UUID.class), any(UUID.class), any(UpdateMissionCommand.class)))
        .thenReturn(
            new Mission(
                missionId,
                accountId,
                "Updated title",
                "Lead developer",
                "Updated description",
                "in_progress",
                "high",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-01T11:00:00Z")));

    ResponseEntity<MissionResponseDto> response =
        controller.updateMyMission(
            principal,
            missionId,
            Map.of(
                "title", "Updated title",
                "description", "Updated description"));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Updated title", response.getBody().title());
    assertEquals("Updated description", response.getBody().description());
  }

  @Test
  void deleteMyMissionShouldReturnNoContent() {
    MissionController controller =
        new MissionController(
            createMissionUseCase,
            getAccountUseCase,
            getMissionUseCase,
            listMissionUseCase,
            updateMissionUseCase,
            deleteMissionUseCase);
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

    ResponseEntity<Void> response = controller.deleteMyMission(principal, missionId);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }
}
