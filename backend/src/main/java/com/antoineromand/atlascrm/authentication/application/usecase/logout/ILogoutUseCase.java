package com.antoineromand.atlascrm.authentication.application.usecase.logout;

public interface ILogoutUseCase {
  void execute(String refreshToken);
}
