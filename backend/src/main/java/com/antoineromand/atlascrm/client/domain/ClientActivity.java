package com.antoineromand.atlascrm.client.domain;

import java.time.Instant;
import java.util.UUID;

public class ClientActivity {
  private final UUID id;
  private final UUID clientId;
  private final String activityType;
  private final String title;
  private final String description;
  private final Instant occurredAt;
  private final Instant createdAt;
  private final Instant updatedAt;

  public ClientActivity(
      UUID id,
      UUID clientId,
      String activityType,
      String title,
      String description,
      Instant occurredAt,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.clientId = clientId;
    this.activityType = activityType;
    this.title = title;
    this.description = description;
    this.occurredAt = occurredAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getClientId() {
    return clientId;
  }

  public String getActivityType() {
    return activityType;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
