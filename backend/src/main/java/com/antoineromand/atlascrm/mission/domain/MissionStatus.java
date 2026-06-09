package com.antoineromand.atlascrm.mission.domain;

import java.util.Locale;

public enum MissionStatus {
  CREATED(0),
  ANALYSED(10),
  PLANNED(20),
  STARTED(30),
  IN_PROGRESS(60),
  FINALIZED(80),
  SHIPPED(95),
  COMPLETED(100);

  private final int progress;

  MissionStatus(int progress) {
    this.progress = progress;
  }

  public int progress() {
    return progress;
  }

  public String value() {
    return name().toLowerCase(Locale.ROOT);
  }

  public boolean isCompleted() {
    return this == COMPLETED;
  }

  public static MissionStatus fromValue(String value) {
    if (value == null || value.isBlank()) {
      return CREATED;
    }

    String normalized = value.trim().toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "NOT_STARTED", "CREATED" -> CREATED;
      case "ANALYSED" -> ANALYSED;
      case "PLANNED" -> PLANNED;
      case "STARTED" -> STARTED;
      case "IN_PROGRESS" -> IN_PROGRESS;
      case "FINALIZED" -> FINALIZED;
      case "SHIPPED" -> SHIPPED;
      case "COMPLETED" -> COMPLETED;
      default -> throw new IllegalArgumentException("Unknown mission status: " + value);
    };
  }
}
