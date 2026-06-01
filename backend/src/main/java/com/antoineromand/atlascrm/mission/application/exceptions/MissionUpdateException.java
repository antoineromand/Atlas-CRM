package com.antoineromand.atlascrm.mission.application.exceptions;

public class MissionUpdateException extends RuntimeException {
  private final String code;

  public MissionUpdateException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
