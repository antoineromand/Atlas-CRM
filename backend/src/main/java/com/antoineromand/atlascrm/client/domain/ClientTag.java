package com.antoineromand.atlascrm.client.domain;

import java.time.Instant;
import java.util.UUID;

public class ClientTag {
  private final UUID id;
  private final UUID accountId;
  private final String name;
  private final String color;
  private final Instant createdAt;
  private final Instant updatedAt;

  public ClientTag(
      UUID id,
      UUID accountId,
      String name,
      String color,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.accountId = accountId;
    this.name = name;
    this.color = color;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
