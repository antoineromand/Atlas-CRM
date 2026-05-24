package com.antoineromand.atlascrm.account.application.exceptions;

import com.antoineromand.atlascrm.authentication.application.exceptions.AuthenticationException;

public class AccountNotFoundException extends AuthenticationException {
  public AccountNotFoundException() {
    super("ACCOUNT_NOT_FOUND", "Account not found.");
  }
}
