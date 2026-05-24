package com.antoineromand.atlascrm.authentication.application.exceptions;

public class CredentialsNotFoundException extends AuthenticationException {
  public CredentialsNotFoundException() {
    super("CREDENTIALS_NOT_FOUND", "Credentials not found.");
  }
}
