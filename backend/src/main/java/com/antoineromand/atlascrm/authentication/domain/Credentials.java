package com.antoineromand.atlascrm.authentication.domain;

import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import java.time.Instant;
import java.util.UUID;

public class Credentials {
  private final UUID id;
  private final String email;
  private String password;
  private final RoleName role;
  private final Instant createdAt;
  private Instant updatedAt;
  private CredentialsStatus status;
  private boolean emailVerified;

  public Credentials(
      UUID id,
      String email,
      String password,
      RoleName role,
      Instant createdAt,
      Instant updatedAt,
      CredentialsStatus status,
      boolean emailVerified) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.role = role;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.status = status;
    this.emailVerified = emailVerified;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public RoleName getRole() {
    return role;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public CredentialsStatus getStatus() {
    return status;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }

  public boolean isActive() {
    return this.status == CredentialsStatus.ACTIVE;
  }

  public void activate() {
    this.status = CredentialsStatus.ACTIVE;
    this.updatedAt = Instant.now();
  }

  public void suspend() {
    this.status = CredentialsStatus.SUSPENDED;
    this.updatedAt = Instant.now();
  }

  public void verifyEmail() {
    this.emailVerified = true;
    this.updatedAt = Instant.now();
  }

  public void changePassword(String password) {
    this.password = password;
    this.updatedAt = Instant.now();
  }
}
