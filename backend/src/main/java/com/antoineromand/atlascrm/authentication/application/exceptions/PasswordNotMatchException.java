package com.antoineromand.atlascrm.authentication.application.exceptions;

public class PasswordNotMatchException extends AuthenticationException {
  public PasswordNotMatchException() {
    super("INVALID_CREDENTIALS", "Invalid email or password.");
  }
}
