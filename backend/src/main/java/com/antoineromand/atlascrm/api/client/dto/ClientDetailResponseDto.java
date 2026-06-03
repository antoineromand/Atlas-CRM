package com.antoineromand.atlascrm.api.client.dto;

import java.util.List;

public record ClientDetailResponseDto(
    ClientResponseDto client,
    List<ClientContactResponseDto> contacts,
    List<ClientActivityResponseDto> activities,
    List<ClientTagResponseDto> tags) {}
