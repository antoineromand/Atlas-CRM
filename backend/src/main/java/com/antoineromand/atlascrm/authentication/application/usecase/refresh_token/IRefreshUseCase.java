package com.antoineromand.atlascrm.authentication.application.usecase.refresh_token;

import com.antoineromand.atlascrm.authentication.application.TokenPair;

public interface IRefreshUseCase {
  TokenPair execute(String token);
}
