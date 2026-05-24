package com.antoineromand.atlascrm.authentication.application.usecase.register;

import com.antoineromand.atlascrm.authentication.application.exceptions.EmailAlreadyUsedException;
import com.antoineromand.atlascrm.authentication.application.exceptions.AuthenticationException;
import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.authentication.domain.repository.IProfileRepository;
import com.antoineromand.atlascrm.authentication.domain.service.IPasswordService;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterUseCase implements IRegisterUseCase {
  private final ICredentialsRepository credentialsRepository;
  private final IProfileRepository profileRepository;
  private final IPasswordService passwordService;

  public RegisterUseCase(
      ICredentialsRepository credentialsRepository,
      IProfileRepository profileRepository,
      IPasswordService passwordService) {
    this.credentialsRepository = credentialsRepository;
    this.profileRepository = profileRepository;
    this.passwordService = passwordService;
  }

  @Override
  public UUID execute(RegisterCommand command) {
    this.ensureEmailIsUnique(command.email());

    String hashedPassword = this.hashPassword(command.password());
    Credentials credentials = this.createCredentials(command, hashedPassword);
    UUID credentialsId = this.persistCredentials(credentials);
    this.createUserProfile(credentialsId, command);
    return credentialsId;
  }

  private void ensureEmailIsUnique(String email) {
    if (this.credentialsRepository.findByEmail(email).isPresent()) {
      throw new EmailAlreadyUsedException();
    }
  }

  private String hashPassword(String plainPassword) {
    return this.passwordService.hashPassword(plainPassword);
  }

  private Credentials createCredentials(RegisterCommand command, String hashedPassword) {
    return new Credentials(
        null,
        command.email(),
        hashedPassword,
        RoleName.USER,
        Instant.now(),
        null,
        CredentialsStatus.ACTIVE,
        false);
  }

  private UUID persistCredentials(Credentials credentials) {
    try {
      return this.credentialsRepository.save(credentials);
    } catch (DataIntegrityViolationException ex) {
      throw new AuthenticationException(
          "DUPLICATED_CREDENTIALS", "A credentials record already exists.");
    }
  }

  private void createUserProfile(UUID credentialsId, RegisterCommand command) {
    Profile profile =
        new Profile(
            null,
            credentialsId,
            command.firstName(),
            command.lastName(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Instant.now(),
            null);
    this.profileRepository.save(profile);
  }
}
