package com.antoineromand.atlascrm.api.mission.dto;

import java.util.List;

public record MissionPageResponseDto(
    List<MissionResponseDto> items,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious) {}
