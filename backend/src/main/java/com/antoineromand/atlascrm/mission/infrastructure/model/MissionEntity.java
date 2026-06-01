package com.antoineromand.atlascrm.mission.infrastructure.model;

import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(
    name = "missions",
    indexes = {
      @Index(name = "idx_missions_account_id", columnList = "account_id"),
      @Index(name = "idx_missions_status", columnList = "status"),
      @Index(name = "idx_missions_priority", columnList = "priority"),
      @Index(name = "idx_missions_deadline", columnList = "deadline")
    })
@Entity
@EntityListeners(AuditingEntityListener.class)
public class MissionEntity {
  @Id
  @Column(name = "mission_id", columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private AccountEntity account;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "role_in_project", length = 150)
  private String roleInProject;

  @Column(name = "description")
  private String description;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "priority", nullable = false, length = 16)
  private String priority;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "deadline")
  private LocalDate deadline;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public MissionEntity() {}

  public MissionEntity(
      UUID id,
      AccountEntity account,
      String title,
      String roleInProject,
      String description,
      String status,
      String priority,
      LocalDate startDate,
      LocalDate deadline,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id != null ? id : UUID.randomUUID();
    this.account = account;
    this.title = title;
    this.roleInProject = roleInProject;
    this.description = description;
    this.status = status;
    this.priority = priority;
    this.startDate = startDate;
    this.deadline = deadline;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static MissionEntity fromDomain(Mission mission, AccountEntity account) {
    return new MissionEntity(
        mission.getId(),
        account,
        mission.getTitle(),
        mission.getRoleInProject(),
        mission.getDescription(),
        mission.getStatus(),
        mission.getPriority(),
        mission.getStartDate(),
        mission.getDeadline(),
        mission.getCreatedAt(),
        mission.getUpdatedAt());
  }

  public Mission toDomain() {
    return new Mission(
        id,
        account.getId(),
        title,
        roleInProject,
        description,
        status,
        priority,
        startDate,
        deadline,
        createdAt,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public AccountEntity getAccount() {
    return account;
  }

  public String getTitle() {
    return title;
  }

  public String getRoleInProject() {
    return roleInProject;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }

  public String getPriority() {
    return priority;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
