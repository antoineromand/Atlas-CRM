package com.antoineromand.atlascrm.api.client.dto;

import java.util.List;
import com.antoineromand.atlascrm.api.mission.dto.MissionResponseDto;

public record ClientDetailResponseDto(
    ClientResponseDto client,
    List<ClientContactResponseDto> contacts,
    List<ClientActivityResponseDto> activities,
    List<ClientTagResponseDto> tags,
    List<MissionResponseDto> missions) {}
