package com.antoineromand.atlascrm.authentication.infrastructure.external.encryption;

import com.antoineromand.atlascrm.authentication.domain.service.IPasswordService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService implements IPasswordService {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public String hashPassword(String password) {
    return this.encoder.encode(password);
  }

  @Override
  public boolean matches(String raw, String hashed) {
    return this.encoder.matches(raw, hashed);
  }
}
