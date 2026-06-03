package com.antoineromand.atlascrm.client.application.usecase.create;

import java.util.UUID;

public record CreateClientCommand(
    UUID accountId,
    String companyName,
    String status,
    String notes) {}
