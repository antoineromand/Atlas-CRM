package com.antoineromand.atlascrm.authentication.application.usecase.login;

import com.antoineromand.atlascrm.authentication.application.TokenPair;

public interface ILoginUseCase {
  TokenPair execute(LoginCommand command);
}
