package com.antoineromand.atlascrm.client.application.exceptions;

public class ClientCreationException extends RuntimeException {
  private final String code;

  public ClientCreationException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
