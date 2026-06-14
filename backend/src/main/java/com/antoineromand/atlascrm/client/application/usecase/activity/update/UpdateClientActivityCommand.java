package com.antoineromand.atlascrm.client.application.usecase.activity.update;

import java.time.Instant;

public record UpdateClientActivityCommand(
    String activityType, String title, String description, Instant occurredAt) {}
