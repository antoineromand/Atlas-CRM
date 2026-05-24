package com.antoineromand.atlascrm.authentication.application.usecase.register;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.authentication.application.exceptions.EmailAlreadyUsedException;
import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.account.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.account.domain.repository.IProfileRepository;
import com.antoineromand.atlascrm.authentication.domain.service.IPasswordService;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

  @Mock private ICredentialsRepository credentialsRepository;
  @Mock private IProfileRepository profileRepository;
  @Mock private IPasswordService passwordService;

  @InjectMocks private RegisterUseCase registerUseCase;

  @Test
  void executeShouldPersistCredentialsAndCreateProfileWithIdentity() {
    RegisterCommand command =
        new RegisterCommand("user@example.com", "Password123!", "John", "Doe");
    UUID credentialsId = UUID.randomUUID();

    when(credentialsRepository.findByEmail(command.email())).thenReturn(Optional.empty());
    when(passwordService.hashPassword(command.password())).thenReturn("hashed-password");
    when(credentialsRepository.save(any(Credentials.class))).thenReturn(credentialsId);
    when(profileRepository.save(any(Profile.class))).thenReturn(UUID.randomUUID());

    UUID result = registerUseCase.execute(command);

    assertEquals(credentialsId, result);
    ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository).save(profileCaptor.capture());
    Profile savedProfile = profileCaptor.getValue();
    assertEquals(credentialsId, savedProfile.getCredentialsId());
    assertEquals("John", savedProfile.getFirstName());
    assertEquals("Doe", savedProfile.getLastName());
    assertEquals(null, savedProfile.getCompanyName());
    verify(credentialsRepository).save(any(Credentials.class));
  }

  @Test
  void executeShouldRejectDuplicateEmail() {
    RegisterCommand command =
        new RegisterCommand("user@example.com", "Password123!", "John", "Doe");

    when(credentialsRepository.findByEmail(command.email()))
        .thenReturn(Optional.of(new Credentials(
            UUID.randomUUID(),
            command.email(),
            "hashed-password",
            RoleName.USER,
            Instant.now(),
            null,
            CredentialsStatus.ACTIVE,
            true)));

    assertThrows(EmailAlreadyUsedException.class, () -> registerUseCase.execute(command));
    verify(passwordService, never()).hashPassword(any());
    verify(credentialsRepository, never()).save(any());
    verify(profileRepository, never()).save(any());
  }

  @Test
  void executeShouldPropagateProfileCreationAfterCredentialsSave() {
    RegisterCommand command =
        new RegisterCommand("user@example.com", "Password123!", "John", "Doe");
    UUID credentialsId = UUID.randomUUID();

    when(credentialsRepository.findByEmail(command.email())).thenReturn(Optional.empty());
    when(passwordService.hashPassword(command.password())).thenReturn("hashed-password");
    when(credentialsRepository.save(any(Credentials.class))).thenReturn(credentialsId);
    when(profileRepository.save(any(Profile.class))).thenReturn(UUID.randomUUID());

    registerUseCase.execute(command);

    verify(credentialsRepository).save(any(Credentials.class));
    verify(profileRepository).save(any(Profile.class));
  }
}
