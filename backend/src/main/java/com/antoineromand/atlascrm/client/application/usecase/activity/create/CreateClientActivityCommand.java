package com.antoineromand.atlascrm.client.application.usecase.activity.create;

import java.time.Instant;

public record CreateClientActivityCommand(
    String activityType, String title, String description, Instant occurredAt) {}
