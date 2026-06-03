package com.antoineromand.atlascrm.client.application.usecase.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateClientUseCaseTest {

  @Mock private IClientRepository clientRepository;

  @Test
  void executeShouldMergeFieldsAndReturnUpdatedClient() {
    UpdateClientUseCase useCase = new UpdateClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Instant updatedAt = Instant.parse("2026-06-01T11:00:00Z");

    Client existing =
        new Client(
            clientId,
            accountId,
            "Initial company",
            "prospect",
            "Initial notes",
            createdAt,
            null);
    Client saved =
        new Client(
            clientId,
            accountId,
            "Updated company",
            "active",
            "Updated notes",
            createdAt,
            updatedAt);

    when(clientRepository.findByIdAndAccountId(clientId, accountId))
        .thenReturn(Optional.of(existing), Optional.of(saved));
    when(clientRepository.save(any(Client.class))).thenReturn(clientId);

    Client result =
        useCase.execute(
            accountId,
            clientId,
            new UpdateClientCommand(
                PatchValue.of("Updated company"),
                PatchValue.of("active"),
                PatchValue.of("Updated notes")));

    ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
    verify(clientRepository).save(captor.capture());
    Client persisted = captor.getValue();

    assertEquals(clientId, result.getId());
    assertEquals("Updated company", persisted.getCompanyName());
    assertEquals("active", persisted.getStatus());
    assertEquals("Updated notes", persisted.getNotes());
    assertEquals(createdAt, persisted.getCreatedAt());
  }

  @Test
  void executeShouldThrowWhenClientDoesNotExistForAccount() {
    UpdateClientUseCase useCase = new UpdateClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.empty());

    assertThrows(
        ClientNotFoundException.class,
        () ->
            useCase.execute(
                accountId,
                clientId,
                new UpdateClientCommand(
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent())));
  }
}
