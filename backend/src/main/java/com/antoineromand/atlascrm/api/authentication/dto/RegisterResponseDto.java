package com.antoineromand.atlascrm.api.authentication.dto;

import java.util.UUID;

public record RegisterResponseDto(UUID credentialsId, String message) {}
