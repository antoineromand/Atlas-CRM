package com.antoineromand.atlascrm.authentication.application.exceptions;

public class CredentialsNotActiveException extends AuthenticationException {
  public CredentialsNotActiveException() {
    super("CREDENTIALS_NOT_ACTIVE", "The account is suspended.");
  }
}
