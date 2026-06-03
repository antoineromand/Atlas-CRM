package com.antoineromand.atlascrm.client.domain;

import java.time.Instant;
import java.util.UUID;

public class Client {
  private final UUID id;
  private final UUID accountId;
  private final String companyName;
  private final String status;
  private final String notes;
  private final Instant createdAt;
  private final Instant updatedAt;

  public Client(
      UUID id,
      UUID accountId,
      String companyName,
      String status,
      String notes,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.accountId = accountId;
    this.companyName = companyName;
    this.status = status;
    this.notes = notes;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getStatus() {
    return status;
  }

  public String getNotes() {
    return notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
