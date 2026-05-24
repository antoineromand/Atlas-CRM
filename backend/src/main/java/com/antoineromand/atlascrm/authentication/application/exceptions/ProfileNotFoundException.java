package com.antoineromand.atlascrm.authentication.application.exceptions;

public class ProfileNotFoundException extends AuthenticationException {
  public ProfileNotFoundException() {
    super("PROFILE_NOT_FOUND", "Profile not found.");
  }
}
