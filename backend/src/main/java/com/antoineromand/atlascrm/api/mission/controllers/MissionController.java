package com.antoineromand.atlascrm.api.mission.controllers;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionRequestDto;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionResponseDto;
import com.antoineromand.atlascrm.mission.application.usecase.create.CreateMissionCommand;
import com.antoineromand.atlascrm.mission.application.usecase.create.ICreateMissionUseCase;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/missions")
public class MissionController {

  private final ICreateMissionUseCase createMissionUseCase;
  private final IGetAccountUseCase getAccountUseCase;

  public MissionController(
      ICreateMissionUseCase createMissionUseCase, IGetAccountUseCase getAccountUseCase) {
    this.createMissionUseCase = createMissionUseCase;
    this.getAccountUseCase = getAccountUseCase;
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

  private UUID extractCredentialsId(Principal principal) {
    return UUID.fromString(principal.getName());
  }
}
