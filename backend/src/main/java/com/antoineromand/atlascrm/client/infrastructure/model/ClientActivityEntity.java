package com.antoineromand.atlascrm.client.infrastructure.model;

import com.antoineromand.atlascrm.client.domain.ClientActivity;
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

@Table(name = "client_activities")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ClientActivityEntity {
  @Id
  @Column(name = "activity_id", columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private ClientEntity client;

  @Column(name = "activity_type", nullable = false, length = 32)
  private String activityType;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public ClientActivityEntity() {}

  public ClientActivityEntity(
      UUID id,
      ClientEntity client,
      String activityType,
      String title,
      String description,
      Instant occurredAt,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id != null ? id : UUID.randomUUID();
    this.client = client;
    this.activityType = activityType;
    this.title = title;
    this.description = description;
    this.occurredAt = occurredAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static ClientActivityEntity fromDomain(ClientActivity activity, ClientEntity client) {
    return new ClientActivityEntity(
        activity.getId(),
        client,
        activity.getActivityType(),
        activity.getTitle(),
        activity.getDescription(),
        activity.getOccurredAt(),
        activity.getCreatedAt(),
        activity.getUpdatedAt());
  }

  public ClientActivity toDomain() {
    return new ClientActivity(
        id,
        client.getId(),
        activityType,
        title,
        description,
        occurredAt,
        createdAt,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public ClientEntity getClient() {
    return client;
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
