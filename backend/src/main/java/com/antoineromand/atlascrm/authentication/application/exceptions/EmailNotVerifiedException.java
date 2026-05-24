package com.antoineromand.atlascrm.authentication.application.exceptions;

public class EmailNotVerifiedException extends AuthenticationException {
  public EmailNotVerifiedException() {
    super("EMAIL_NOT_VERIFIED", "Email address must be verified before sign-in.");
  }
}
