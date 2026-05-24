package com.antoineromand.atlascrm.api.account.dto;

import java.time.Instant;
import java.util.UUID;

public record AccountResponseDto(
    UUID id,
    UUID credentialsId,
    String firstName,
    String lastName,
    String companyName,
    String siretNumber,
    String vatNumber,
    String billingEmail,
    String billingAddressLine1,
    String billingAddressLine2,
    String billingPostalCode,
    String billingCity,
    String billingCountry,
    Instant createdAt,
    Instant updatedAt) {}
