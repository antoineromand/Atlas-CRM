package com.antoineromand.atlascrm.client.application.usecase.activity.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.repository.IClientActivityRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateClientActivityUseCaseTest {

  @Mock private IClientRepository clientRepository;
  @Mock private IClientActivityRepository clientActivityRepository;

  @Test
  void executeShouldUpdateActivity() {
    UpdateClientActivityUseCase useCase =
        new UpdateClientActivityUseCase(clientRepository, clientActivityRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    UUID activityId = UUID.randomUUID();
    Client client =
        new Client(
            clientId,
            accountId,
            "Atlas Studio",
            "Julie",
            "Martin",
            "active",
            "Important client",
            Instant.parse("2026-06-01T10:00:00Z"),
            null);
    ClientActivity existing =
        new ClientActivity(
            activityId,
            clientId,
            "call",
            "Kickoff call",
            "Initial call",
            Instant.parse("2026-06-01T09:00:00Z"),
            Instant.parse("2026-06-01T09:00:00Z"),
            null);

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientActivityRepository.findById(activityId))
        .thenReturn(
            Optional.of(existing),
            Optional.of(
                new ClientActivity(
                    activityId,
                    clientId,
                    "meeting",
                    "Client review",
                    "Updated notes",
                    Instant.parse("2026-06-01T11:00:00Z"),
                    Instant.parse("2026-06-01T09:00:00Z"),
                    Instant.parse("2026-06-01T11:00:00Z"))));
    when(clientActivityRepository.save(org.mockito.ArgumentMatchers.any(ClientActivity.class)))
        .thenReturn(activityId);

    ClientActivity result =
        useCase.execute(
            accountId,
            clientId,
            activityId,
            new UpdateClientActivityCommand(
                "meeting",
                "Client review",
                "Updated notes",
                Instant.parse("2026-06-01T11:00:00Z")));

    assertEquals("meeting", result.getActivityType());
  }

  @Test
  void executeShouldRejectActivityFromAnotherClient() {
    UpdateClientActivityUseCase useCase =
        new UpdateClientActivityUseCase(clientRepository, clientActivityRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    UUID activityId = UUID.randomUUID();
    UUID otherClientId = UUID.randomUUID();
    Client client =
        new Client(
            clientId,
            accountId,
            "Atlas Studio",
            "Julie",
            "Martin",
            "active",
            "Important client",
            Instant.parse("2026-06-01T10:00:00Z"),
            null);

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientActivityRepository.findById(activityId))
        .thenReturn(
            Optional.of(
                new ClientActivity(
                    activityId,
                    otherClientId,
                    "call",
                    "Kickoff call",
                    "Initial call",
                    Instant.parse("2026-06-01T09:00:00Z"),
                    Instant.parse("2026-06-01T09:00:00Z"),
                    null)));

    assertThrows(
        ClientNotFoundException.class,
        () ->
            useCase.execute(
                accountId,
                clientId,
                activityId,
                new UpdateClientActivityCommand("meeting", "Client review", null, null)));
  }
}
