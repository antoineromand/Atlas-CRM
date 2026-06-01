package com.antoineromand.atlascrm.mission.application.exceptions;

public class MissionNotFoundException extends RuntimeException {
  private final String code;

  public MissionNotFoundException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
