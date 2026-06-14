package com.antoineromand.atlascrm.api.client.dto;

import jakarta.validation.constraints.Size;

public record UpdateClientContactRequestDto(
    @Size(max = 120) String firstName,
    @Size(max = 120) String lastName,
    @Size(max = 200) String email,
    @Size(max = 30) String phone,
    @Size(max = 120) String jobTitle,
    boolean primary) {}
