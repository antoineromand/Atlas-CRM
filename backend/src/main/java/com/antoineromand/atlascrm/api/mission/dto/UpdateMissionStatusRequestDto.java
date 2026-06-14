package com.antoineromand.atlascrm.api.mission.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMissionStatusRequestDto(@NotBlank String status) {}
