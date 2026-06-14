package com.antoineromand.atlascrm.client.infrastructure.model;

import com.antoineromand.atlascrm.client.domain.ClientContact;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "client_contacts")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ClientContactEntity {
  @Id
  @Column(name = "contact_id", columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private ClientEntity client;

  @Column(name = "first_name", length = 120)
  private String firstName;

  @Column(name = "last_name", length = 120)
  private String lastName;

  @Column(name = "email", length = 200)
  private String email;

  @Column(name = "phone", length = 30)
  private String phone;

  @Column(name = "job_title", length = 120)
  private String jobTitle;

  @Column(name = "is_primary", nullable = false)
  private boolean primary;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public ClientContactEntity() {}

  public ClientContactEntity(
      UUID id,
      ClientEntity client,
      String firstName,
      String lastName,
      String email,
      String phone,
      String jobTitle,
      boolean primary,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id != null ? id : UUID.randomUUID();
    this.client = client;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.jobTitle = jobTitle;
    this.primary = primary;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static ClientContactEntity fromDomain(ClientContact contact, ClientEntity client) {
    return new ClientContactEntity(
        contact.getId(),
        client,
        contact.getFirstName(),
        contact.getLastName(),
        contact.getEmail(),
        contact.getPhone(),
        contact.getJobTitle(),
        contact.isPrimary(),
        contact.getCreatedAt(),
        contact.getUpdatedAt());
  }

  public ClientContact toDomain() {
    return new ClientContact(
        id,
        client.getId(),
        firstName,
        lastName,
        email,
        phone,
        jobTitle,
        primary,
        createdAt,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public ClientEntity getClient() {
    return client;
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

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
