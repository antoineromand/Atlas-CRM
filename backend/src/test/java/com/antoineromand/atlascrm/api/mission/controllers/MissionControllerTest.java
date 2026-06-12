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
import com.antoineromand.atlascrm.api.mission.dto.UpdateMissionStatusRequestDto;
import com.antoineromand.atlascrm.mission.application.usecase.create.CreateMissionCommand;
import com.antoineromand.atlascrm.mission.application.usecase.create.ICreateMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.delete.IDeleteMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.get.IGetMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.list.IListMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.list.ListMissionQuery;
import com.antoineromand.atlascrm.mission.application.usecase.summary.IGetMissionSummaryUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.summary.MissionSummaryResult;
import com.antoineromand.atlascrm.mission.application.usecase.update.IUpdateMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.update.IUpdateMissionStatusUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.update.UpdateMissionCommand;
import com.antoineromand.atlascrm.mission.application.usecase.update.UpdateMissionStatusCommand;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.MissionStatus;
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
  @Mock private IGetMissionSummaryUseCase getMissionSummaryUseCase;
  @Mock private IUpdateMissionUseCase updateMissionUseCase;
  @Mock private IUpdateMissionStatusUseCase updateMissionStatusUseCase;
  @Mock private IDeleteMissionUseCase deleteMissionUseCase;

  private MissionController createController() {
    return new MissionController(
        createMissionUseCase,
        getAccountUseCase,
        getMissionUseCase,
        listMissionUseCase,
        getMissionSummaryUseCase,
        updateMissionUseCase,
        updateMissionStatusUseCase,
        deleteMissionUseCase);
  }

  @Test
  void createMissionShouldResolveCurrentAccountAndReturnCreatedResponse() {
    MissionController controller =
        createController();
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID missionId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
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
                clientId,
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
    assertEquals(clientId, command.clientId());
    assertEquals("Website redesign", command.title());
    assertEquals("Lead developer", command.roleInProject());
    assertEquals("Redesign the marketing website", command.description());
    assertEquals("high", command.priority());
    assertEquals(LocalDate.of(2026, 6, 1), command.startDate());
    assertEquals(LocalDate.of(2026, 6, 30), command.deadline());
  }

  @Test
  void listMyMissionsShouldReturnMissionDtos() {
    MissionController controller = createController();
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
    when(listMissionUseCase.execute(new ListMissionQuery(accountId, null, 1, 6)))
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
    MissionController controller = createController();
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
    when(listMissionUseCase.execute(new ListMissionQuery(accountId, "website", 1, 6)))
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
    MissionController controller = createController();
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
  void getMyMissionSummaryShouldReturnGlobalStats() {
    MissionController controller = createController();
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
    when(getMissionSummaryUseCase.execute(accountId))
        .thenReturn(new MissionSummaryResult(5, 3, 2, 1, 4));

    ResponseEntity<com.antoineromand.atlascrm.api.mission.dto.MissionSummaryResponseDto> response =
        controller.getMyMissionSummary(principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(5L, response.getBody().totalMissions());
    assertEquals(3L, response.getBody().activeMissions());
    assertEquals(2L, response.getBody().completedMissions());
    assertEquals(1L, response.getBody().dueSoonMissions());
    assertEquals(4L, response.getBody().highPriorityMissions());
  }

  @Test
  void getMyMissionShouldReturnMissionDto() {
    MissionController controller = createController();
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
    MissionController controller = createController();
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
  void updateMyMissionStatusShouldUseDedicatedRoute() {
    MissionController controller = createController();
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
    when(updateMissionStatusUseCase.execute(any(UpdateMissionStatusCommand.class)))
        .thenReturn(
            new Mission(
                missionId,
                accountId,
                null,
                "Updated title",
                "Lead developer",
                "Updated description",
                MissionStatus.IN_PROGRESS,
                "high",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-01T11:00:00Z")));

    ResponseEntity<MissionResponseDto> response =
        controller.updateMyMissionStatus(principal, missionId, new UpdateMissionStatusRequestDto("in_progress"));

    ArgumentCaptor<UpdateMissionStatusCommand> captor =
        ArgumentCaptor.forClass(UpdateMissionStatusCommand.class);
    verify(updateMissionStatusUseCase).execute(captor.capture());

    UpdateMissionStatusCommand command = captor.getValue();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("in_progress", response.getBody().status());
    assertEquals(accountId, command.accountId());
    assertEquals(missionId, command.missionId());
    assertEquals("in_progress", command.status());
  }

  @Test
  void deleteMyMissionShouldReturnNoContent() {
    MissionController controller = createController();
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
