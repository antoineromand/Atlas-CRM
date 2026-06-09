package com.antoineromand.atlascrm.mission.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Mission {
  private final UUID id;
  private final UUID accountId;
  private final String title;
  private final String roleInProject;
  private final String description;
  private final MissionStatus status;
  private final String priority;
  private final LocalDate startDate;
  private final LocalDate deadline;
  private final Instant createdAt;
  private final Instant updatedAt;

  public Mission(
      UUID id,
      UUID accountId,
      String title,
      String roleInProject,
      String description,
      MissionStatus status,
      String priority,
      LocalDate startDate,
      LocalDate deadline,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.accountId = accountId;
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

  public Mission(
      UUID id,
      UUID accountId,
      String title,
      String roleInProject,
      String description,
      String status,
      String priority,
      LocalDate startDate,
      LocalDate deadline,
      Instant createdAt,
      Instant updatedAt) {
    this(
        id,
        accountId,
        title,
        roleInProject,
        description,
        MissionStatus.fromValue(status),
        priority,
        startDate,
        deadline,
        createdAt,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
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
    return status.value();
  }

  public MissionStatus getMissionStatus() {
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

  public int getProgress() {
    return status.progress();
  }
}
