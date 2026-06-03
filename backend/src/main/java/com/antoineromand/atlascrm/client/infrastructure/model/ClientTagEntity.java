package com.antoineromand.atlascrm.client.infrastructure.model;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.client.domain.ClientTag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(
    name = "client_tags",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"account_id", "name"})
    },
    indexes = {
      @Index(name = "idx_client_tags_account_id", columnList = "account_id"),
      @Index(name = "idx_client_tags_name", columnList = "name")
    })
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ClientTagEntity {
  @Id
  @Column(name = "tag_id", columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private AccountEntity account;

  @Column(name = "name", nullable = false, length = 80)
  private String name;

  @Column(name = "color", length = 20)
  private String color;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "client_tag_links",
      joinColumns = @JoinColumn(name = "tag_id"),
      inverseJoinColumns = @JoinColumn(name = "client_id"))
  private Set<ClientEntity> clients = new HashSet<>();

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public ClientTagEntity() {}

  public ClientTagEntity(
      UUID id,
      AccountEntity account,
      String name,
      String color,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id != null ? id : UUID.randomUUID();
    this.account = account;
    this.name = name;
    this.color = color;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static ClientTagEntity fromDomain(ClientTag tag, AccountEntity account) {
    return new ClientTagEntity(
        tag.getId(), account, tag.getName(), tag.getColor(), tag.getCreatedAt(), tag.getUpdatedAt());
  }

  public ClientTag toDomain() {
    return new ClientTag(id, account.getId(), name, color, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public AccountEntity getAccount() {
    return account;
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
