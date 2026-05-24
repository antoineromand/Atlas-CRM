package com.antoineromand.atlascrm.authentication.application.exceptions;

public class EmailAlreadyUsedException extends AuthenticationException {
  public EmailAlreadyUsedException() {
    super("EMAIL_ALREADY_USED", "Email is already used.");
  }
}
