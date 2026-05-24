package com.antoineromand.atlascrm.authentication.application;

import java.time.Duration;

public interface ITokenRepository {
  void storeRefreshToken(String jti, String userId, Duration duration);

  String getUserIdByJti(String jti);

  void deleteByJti(String jti);
}
