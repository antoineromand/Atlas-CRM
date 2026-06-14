package com.antoineromand.atlascrm.client.application.usecase.activity.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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
class DeleteClientActivityUseCaseTest {

  @Mock private IClientRepository clientRepository;
  @Mock private IClientActivityRepository clientActivityRepository;

  @Test
  void executeShouldDeleteActivity() {
    DeleteClientActivityUseCase useCase =
        new DeleteClientActivityUseCase(clientRepository, clientActivityRepository);
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

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientActivityRepository.findById(activityId))
        .thenReturn(
            Optional.of(
                new ClientActivity(
                    activityId,
                    clientId,
                    "call",
                    "Kickoff call",
                    "Initial call",
                    Instant.parse("2026-06-01T09:00:00Z"),
                    Instant.parse("2026-06-01T09:00:00Z"),
                    null)));

    useCase.execute(accountId, clientId, activityId);

    verify(clientActivityRepository).deleteById(activityId);
  }

  @Test
  void executeShouldRejectUnknownActivity() {
    DeleteClientActivityUseCase useCase =
        new DeleteClientActivityUseCase(clientRepository, clientActivityRepository);
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

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientActivityRepository.findById(activityId)).thenReturn(Optional.empty());

    assertThrows(
        ClientNotFoundException.class, () -> useCase.execute(accountId, clientId, activityId));
  }
}
