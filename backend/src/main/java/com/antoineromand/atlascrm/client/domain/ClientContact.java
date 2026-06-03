package com.antoineromand.atlascrm.client.domain;

import java.time.Instant;
import java.util.UUID;

public class ClientContact {
  private final UUID id;
  private final UUID clientId;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String phone;
  private final String jobTitle;
  private final boolean primary;
  private final Instant createdAt;
  private final Instant updatedAt;

  public ClientContact(
      UUID id,
      UUID clientId,
      String firstName,
      String lastName,
      String email,
      String phone,
      String jobTitle,
      boolean primary,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.clientId = clientId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.jobTitle = jobTitle;
    this.primary = primary;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getClientId() {
    return clientId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public boolean isPrimary() {
    return primary;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
