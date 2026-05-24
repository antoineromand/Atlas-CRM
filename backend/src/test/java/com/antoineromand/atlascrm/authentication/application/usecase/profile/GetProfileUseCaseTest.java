package com.antoineromand.atlascrm.authentication.application.usecase.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.authentication.application.exceptions.ProfileNotFoundException;
import com.antoineromand.atlascrm.authentication.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.repository.IProfileRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProfileUseCaseTest {

  @Mock private IProfileRepository profileRepository;

  @Test
  void executeShouldReturnExistingProfile() {
    GetProfileUseCase useCase = new GetProfileUseCase(profileRepository);
    UUID credentialsId = UUID.randomUUID();
    Profile profile =
        new Profile(
            UUID.randomUUID(),
            credentialsId,
            "John",
            "Doe",
            "JD Consulting",
            "12345678901234",
            "FR12345678901",
            "billing@example.com",
            "10 rue de Paris",
            null,
            "75000",
            "Paris",
            "France",
            Instant.now(),
            null);

    when(profileRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.of(profile));

    Profile result = useCase.execute(credentialsId);

    assertEquals(profile.getId(), result.getId());
    assertEquals(profile.getCredentialsId(), result.getCredentialsId());
    assertEquals(profile.getFirstName(), result.getFirstName());
    assertEquals(profile.getLastName(), result.getLastName());
  }

  @Test
  void executeShouldThrowWhenProfileDoesNotExist() {
    GetProfileUseCase useCase = new GetProfileUseCase(profileRepository);
    UUID credentialsId = UUID.randomUUID();

    when(profileRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.empty());

    assertThrows(ProfileNotFoundException.class, () -> useCase.execute(credentialsId));
  }
}
