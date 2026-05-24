package com.antoineromand.atlascrm.authentication.application.usecase.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.authentication.application.exceptions.ProfileNotFoundException;
import com.antoineromand.atlascrm.authentication.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.repository.IProfileRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

  @Mock private IProfileRepository profileRepository;

  @Test
  void executeShouldMergeFieldsAndReturnUpdatedProfile() {
    UpdateProfileUseCase useCase = new UpdateProfileUseCase(profileRepository);
    UUID credentialsId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    Instant createdAt = Instant.now();
    Profile existing =
        new Profile(
            profileId,
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
            createdAt,
            null);
    Profile updated =
        new Profile(
            profileId,
            credentialsId,
            "Jane",
            "Doe",
            "JD Consulting",
            "12345678901234",
            "FR12345678901",
            "billing@example.com",
            "20 rue de Lyon",
            null,
            "69000",
            "Lyon",
            "France",
            createdAt,
            Instant.now());

    when(profileRepository.findByCredentialsId(credentialsId))
        .thenReturn(Optional.of(existing), Optional.of(updated));
    when(profileRepository.save(any(Profile.class))).thenReturn(profileId);

    Profile result =
        useCase.execute(
            credentialsId,
            new UpdateProfileCommand(
                "Jane",
                null,
                null,
                null,
                null,
                null,
                "20 rue de Lyon",
                null,
                "69000",
                "Lyon",
                null));

    ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository).save(captor.capture());
    Profile saved = captor.getValue();

    assertEquals(profileId, result.getId());
    assertEquals("Jane", saved.getFirstName());
    assertEquals("Doe", saved.getLastName());
    assertEquals("20 rue de Lyon", saved.getBillingAddressLine1());
    assertEquals("69000", saved.getBillingPostalCode());
    assertEquals("Lyon", saved.getBillingCity());
    assertNotNull(result.getUpdatedAt());
  }

  @Test
  void executeShouldThrowWhenProfileDoesNotExist() {
    UpdateProfileUseCase useCase = new UpdateProfileUseCase(profileRepository);
    UUID credentialsId = UUID.randomUUID();

    when(profileRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.empty());

    assertThrows(
        ProfileNotFoundException.class,
        () -> useCase.execute(credentialsId, new UpdateProfileCommand(null, null, null, null, null, null, null, null, null, null, null)));
  }
}
