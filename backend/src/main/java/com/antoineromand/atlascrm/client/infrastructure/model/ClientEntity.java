package com.antoineromand.atlascrm.client.infrastructure.model;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.client.domain.Client;
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

@Table(name = "clients")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ClientEntity {
  @Id
  @Column(name = "client_id", columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private AccountEntity account;

  @Column(name = "company_name", nullable = false, length = 200)
  private String companyName;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "notes", columnDefinition = "text")
  private String notes;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public ClientEntity() {}

  public ClientEntity(
      UUID id,
      AccountEntity account,
      String companyName,
      String status,
      String notes,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id != null ? id : UUID.randomUUID();
    this.account = account;
    this.companyName = companyName;
    this.status = status;
    this.notes = notes;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static ClientEntity fromDomain(Client client, AccountEntity account) {
    return new ClientEntity(
        client.getId(),
        account,
        client.getCompanyName(),
        client.getStatus(),
        client.getNotes(),
        client.getCreatedAt(),
        client.getUpdatedAt());
  }

  public Client toDomain() {
    return new Client(
        id,
        account.getId(),
        companyName,
        status,
        notes,
        createdAt,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public AccountEntity getAccount() {
    return account;
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
