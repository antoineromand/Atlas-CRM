package com.antoineromand.atlascrm.api.client.dto;

import java.util.List;

public record ClientPageResponseDto(
    List<ClientResponseDto> items,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious) {}
