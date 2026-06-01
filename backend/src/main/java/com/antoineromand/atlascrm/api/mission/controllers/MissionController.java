package com.antoineromand.atlascrm.api.mission.controllers;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionRequestDto;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionResponseDto;
import com.antoineromand.atlascrm.api.mission.dto.MissionResponseDto;
import com.antoineromand.atlascrm.mission.application.usecase.create.CreateMissionCommand;
import com.antoineromand.atlascrm.mission.application.usecase.create.ICreateMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.get.IGetMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.list.IListMissionUseCase;
import com.antoineromand.atlascrm.mission.domain.Mission;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/missions")
public class MissionController {

  private final ICreateMissionUseCase createMissionUseCase;
  private final IGetAccountUseCase getAccountUseCase;
  private final IGetMissionUseCase getMissionUseCase;
  private final IListMissionUseCase listMissionUseCase;

  public MissionController(
      ICreateMissionUseCase createMissionUseCase,
      IGetAccountUseCase getAccountUseCase,
      IGetMissionUseCase getMissionUseCase,
      IListMissionUseCase listMissionUseCase) {
    this.createMissionUseCase = createMissionUseCase;
    this.getAccountUseCase = getAccountUseCase;
    this.getMissionUseCase = getMissionUseCase;
    this.listMissionUseCase = listMissionUseCase;
  }

  @PostMapping
  public ResponseEntity<CreateMissionResponseDto> createMission(
      Principal principal, @Valid @RequestBody CreateMissionRequestDto dto) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    UUID missionId =
        this.createMissionUseCase.execute(
            new CreateMissionCommand(
                account.getId(),
                dto.title(),
                dto.roleInProject(),
                dto.description(),
                dto.status(),
                dto.priority(),
                dto.startDate(),
                dto.deadline()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new CreateMissionResponseDto(missionId, "Mission created successfully."));
  }

  @GetMapping
  public ResponseEntity<List<MissionResponseDto>> listMyMissions(Principal principal) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    return ResponseEntity.ok(
        this.listMissionUseCase.execute(account.getId()).stream().map(this::toResponse).toList());
  }

  @GetMapping("/{missionId}")
  public ResponseEntity<MissionResponseDto> getMyMission(
      Principal principal, @PathVariable UUID missionId) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    return ResponseEntity.ok(this.toResponse(this.getMissionUseCase.execute(account.getId(), missionId)));
  }

  private UUID extractCredentialsId(Principal principal) {
    return UUID.fromString(principal.getName());
  }

  private MissionResponseDto toResponse(Mission mission) {
    return new MissionResponseDto(
        mission.getId(),
        mission.getTitle(),
        mission.getRoleInProject(),
        mission.getDescription(),
        mission.getStatus(),
        mission.getPriority(),
        mission.getStartDate(),
        mission.getDeadline(),
        mission.getCreatedAt(),
        mission.getUpdatedAt());
  }
}
