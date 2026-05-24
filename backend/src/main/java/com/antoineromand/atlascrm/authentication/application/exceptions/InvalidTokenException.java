package com.antoineromand.atlascrm.authentication.application.exceptions;

public class InvalidTokenException extends AuthenticationException {
  public InvalidTokenException() {
    super("INVALID_TOKEN", "The token is invalid or expired.");
  }
}
