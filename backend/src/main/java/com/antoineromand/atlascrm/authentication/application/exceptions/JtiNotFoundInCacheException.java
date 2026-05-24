package com.antoineromand.atlascrm.authentication.application.exceptions;

public class JtiNotFoundInCacheException extends AuthenticationException {
  public JtiNotFoundInCacheException() {
    super("TOKEN_REVOKED", "The refresh token has been revoked.");
  }
}
