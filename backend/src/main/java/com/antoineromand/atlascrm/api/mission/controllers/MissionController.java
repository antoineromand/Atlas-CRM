package com.antoineromand.atlascrm.api.mission.controllers;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionRequestDto;
import com.antoineromand.atlascrm.api.mission.dto.CreateMissionResponseDto;
import com.antoineromand.atlascrm.api.mission.dto.MissionPageResponseDto;
import com.antoineromand.atlascrm.api.mission.dto.MissionResponseDto;
import com.antoineromand.atlascrm.api.mission.dto.MissionSummaryResponseDto;
import com.antoineromand.atlascrm.mission.application.usecase.create.CreateMissionCommand;
import com.antoineromand.atlascrm.mission.application.usecase.create.ICreateMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.delete.IDeleteMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.get.IGetMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.list.IListMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.list.MissionPageResult;
import com.antoineromand.atlascrm.mission.application.usecase.summary.IGetMissionSummaryUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.summary.MissionSummaryResult;
import com.antoineromand.atlascrm.mission.application.usecase.update.IUpdateMissionUseCase;
import com.antoineromand.atlascrm.mission.application.usecase.update.UpdateMissionCommand;
import com.antoineromand.atlascrm.mission.domain.Mission;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/missions")
public class MissionController {

  private final ICreateMissionUseCase createMissionUseCase;
  private final IGetAccountUseCase getAccountUseCase;
  private final IGetMissionUseCase getMissionUseCase;
  private final IListMissionUseCase listMissionUseCase;
  private final IGetMissionSummaryUseCase getMissionSummaryUseCase;
  private final IUpdateMissionUseCase updateMissionUseCase;
  private final IDeleteMissionUseCase deleteMissionUseCase;

  public MissionController(
      ICreateMissionUseCase createMissionUseCase,
      IGetAccountUseCase getAccountUseCase,
      IGetMissionUseCase getMissionUseCase,
      IListMissionUseCase listMissionUseCase,
      IGetMissionSummaryUseCase getMissionSummaryUseCase,
      IUpdateMissionUseCase updateMissionUseCase,
      IDeleteMissionUseCase deleteMissionUseCase) {
    this.createMissionUseCase = createMissionUseCase;
    this.getAccountUseCase = getAccountUseCase;
    this.getMissionUseCase = getMissionUseCase;
    this.listMissionUseCase = listMissionUseCase;
    this.getMissionSummaryUseCase = getMissionSummaryUseCase;
    this.updateMissionUseCase = updateMissionUseCase;
    this.deleteMissionUseCase = deleteMissionUseCase;
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
  public ResponseEntity<MissionPageResponseDto> listMyMissions(
      Principal principal,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "6") int size) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    String normalizedSearch = this.normalizeSearch(search);
    int normalizedPage = this.normalizePage(page);
    int normalizedSize = this.normalizeSize(size);
    MissionPageResult result =
        this.listMissionUseCase.execute(account.getId(), normalizedSearch, normalizedPage, normalizedSize);
    return ResponseEntity.ok(this.toPageResponse(result));
  }

  @GetMapping("/summary")
  public ResponseEntity<MissionSummaryResponseDto> getMyMissionSummary(Principal principal) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    MissionSummaryResult result = this.getMissionSummaryUseCase.execute(account.getId());
    return ResponseEntity.ok(this.toSummaryResponse(result));
  }

  @GetMapping("/{missionId}")
  public ResponseEntity<MissionResponseDto> getMyMission(
      Principal principal, @PathVariable UUID missionId) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    return ResponseEntity.ok(this.toResponse(this.getMissionUseCase.execute(account.getId(), missionId)));
  }

  @PatchMapping("/{missionId}")
  public ResponseEntity<MissionResponseDto> updateMyMission(
      Principal principal, @PathVariable UUID missionId, @RequestBody Map<String, Object> body) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    Mission updated =
        this.updateMissionUseCase.execute(
            account.getId(),
            missionId,
            new UpdateMissionCommand(
                this.patchString(body, "title", 200),
                this.patchString(body, "roleInProject", 150),
                this.patchString(body, "description", Integer.MAX_VALUE),
                this.patchString(body, "status", 32),
                this.patchString(body, "priority", 16),
                this.patchDate(body, "startDate"),
                this.patchDate(body, "deadline")));
    return ResponseEntity.ok(this.toResponse(updated));
  }

  @DeleteMapping("/{missionId}")
  public ResponseEntity<Void> deleteMyMission(Principal principal, @PathVariable UUID missionId) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    this.deleteMissionUseCase.execute(account.getId(), missionId);
    return ResponseEntity.noContent().build();
  }

  private UUID extractCredentialsId(Principal principal) {
    return UUID.fromString(principal.getName());
  }

  private String normalizeSearch(String search) {
    if (search == null) {
      return null;
    }

    String normalizedSearch = search.trim();

    if (normalizedSearch.isEmpty()) {
      return null;
    }

    if (normalizedSearch.length() < 3) {
      throw new IllegalArgumentException("search must contain at least 3 characters");
    }

    return normalizedSearch;
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

  private MissionPageResponseDto toPageResponse(MissionPageResult result) {
    return new MissionPageResponseDto(
        result.items().stream().map(this::toResponse).toList(),
        result.page(),
        result.size(),
        result.totalElements(),
        result.totalPages(),
        result.hasNext(),
        result.hasPrevious());
  }

  private MissionSummaryResponseDto toSummaryResponse(MissionSummaryResult result) {
    return new MissionSummaryResponseDto(
        result.totalMissions(),
        result.activeMissions(),
        result.completedMissions(),
        result.dueSoonMissions(),
        result.highPriorityMissions());
  }

  private int normalizePage(int page) {
    if (page < 1) {
      throw new IllegalArgumentException("page must be greater than or equal to 1");
    }

    return page;
  }

  private int normalizeSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("size must be greater than or equal to 1");
    }

    if (size > 50) {
      throw new IllegalArgumentException("size must be lower than or equal to 50");
    }

    return size;
  }

  private PatchValue<String> patchString(Map<String, Object> body, String fieldName, int maxLength) {
    if (!body.containsKey(fieldName)) {
      return PatchValue.absent();
    }

    Object rawValue = body.get(fieldName);
    if (rawValue == null) {
      return PatchValue.of(null);
    }

    if (!(rawValue instanceof String value)) {
      throw new IllegalArgumentException(fieldName + " must be a string or null");
    }

    if (value.length() > maxLength) {
      throw new IllegalArgumentException(fieldName + " exceeds max length of " + maxLength);
    }

    return PatchValue.of(value);
  }

  private PatchValue<LocalDate> patchDate(Map<String, Object> body, String fieldName) {
    if (!body.containsKey(fieldName)) {
      return PatchValue.absent();
    }

    Object rawValue = body.get(fieldName);
    if (rawValue == null) {
      return PatchValue.of(null);
    }

    if (!(rawValue instanceof String value)) {
      throw new IllegalArgumentException(fieldName + " must be an ISO date string or null");
    }

    return PatchValue.of(LocalDate.parse(value));
  }
}
