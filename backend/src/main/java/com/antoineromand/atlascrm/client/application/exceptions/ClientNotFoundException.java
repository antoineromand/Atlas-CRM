package com.antoineromand.atlascrm.client.application.exceptions;

public class ClientNotFoundException extends RuntimeException {
  private final String code;

  public ClientNotFoundException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
