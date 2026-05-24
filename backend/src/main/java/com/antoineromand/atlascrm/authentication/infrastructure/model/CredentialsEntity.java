package com.antoineromand.atlascrm.authentication.infrastructure.model;

import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(
    name = "credentials",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"email"})
    },
    indexes = {
      @Index(name = "idx_credentials_email", columnList = "email")
    })
@Entity
@EntityListeners(AuditingEntityListener.class)
public class CredentialsEntity {
  @jakarta.persistence.Id
  @Column(name = "credentials_id", columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id = UUID.randomUUID();

  @Column(name = "email", nullable = false, length = 200)
  private String email;

  @Column(name = "password", nullable = false, length = 200)
  private String password;

  @Column(name = "created_at", updatable = false, nullable = false)
  @CreatedDate
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_name", nullable = false, length = 50)
  private RoleName roleName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private CredentialsStatus status;

  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified;

  public CredentialsEntity() {}

  public CredentialsEntity(
      UUID id,
      String email,
      String password,
      Instant createdAt,
      Instant updatedAt,
      RoleName roleName,
      CredentialsStatus status,
      boolean emailVerified) {
    this.id = id != null ? id : UUID.randomUUID();
    this.email = email;
    this.password = password;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.roleName = roleName;
    this.status = status;
    this.emailVerified = emailVerified;
  }

  public static CredentialsEntity fromDomain(Credentials credentials) {
    return new CredentialsEntity(
        credentials.getId(),
        credentials.getEmail(),
        credentials.getPassword(),
        credentials.getCreatedAt(),
        credentials.getUpdatedAt(),
        credentials.getRole(),
        credentials.getStatus(),
        credentials.isEmailVerified());
  }

  public Credentials toDomain() {
    return new Credentials(
        id,
        email,
        password,
        roleName,
        createdAt,
        updatedAt,
        status,
        emailVerified);
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public RoleName getRoleName() {
    return roleName;
  }

  public CredentialsStatus getStatus() {
    return status;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }
}
