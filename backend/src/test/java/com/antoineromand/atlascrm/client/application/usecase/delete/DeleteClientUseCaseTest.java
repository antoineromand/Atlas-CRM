package com.antoineromand.atlascrm.client.application.usecase.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteClientUseCaseTest {

  @Mock private IClientRepository clientRepository;

  @Test
  void executeShouldDeleteClientOwnedByAccount() {
    DeleteClientUseCase useCase = new DeleteClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    when(clientRepository.findByIdAndAccountId(clientId, accountId))
        .thenReturn(
            Optional.of(
                new Client(
                    clientId,
                    accountId,
                    "Atlas Studio",
                    "active",
                    null,
                    Instant.now(),
                    null)));

    useCase.execute(accountId, clientId);

    verify(clientRepository).deleteById(clientId);
  }

  @Test
  void executeShouldThrowWhenClientDoesNotExistForAccount() {
    DeleteClientUseCase useCase = new DeleteClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.empty());

    assertThrows(ClientNotFoundException.class, () -> useCase.execute(accountId, clientId));
  }
}
