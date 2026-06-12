package com.antoineromand.atlascrm.client.application.usecase.contact.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.repository.IClientContactRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteClientContactUseCaseTest {

  @Mock private IClientRepository clientRepository;
  @Mock private IClientContactRepository clientContactRepository;

  @Test
  void executeShouldDeleteContact() {
    DeleteClientContactUseCase useCase =
        new DeleteClientContactUseCase(clientRepository, clientContactRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    UUID contactId = UUID.randomUUID();

    Client client =
        new Client(
            clientId,
            accountId,
            "Atlas Studio",
            "active",
            "Notes",
            Instant.parse("2026-06-01T10:00:00Z"),
            null);
    ClientContact contact =
        new ClientContact(
            contactId,
            clientId,
            "Julie",
            "Martin",
            null,
            null,
            null,
            false,
            Instant.parse("2026-06-01T10:00:00Z"),
            null);

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientContactRepository.findById(contactId)).thenReturn(Optional.of(contact));

    useCase.execute(accountId, clientId, contactId);

    verify(clientContactRepository).deleteById(contactId);
  }

  @Test
  void executeShouldThrowWhenContactDoesNotBelongToClient() {
    DeleteClientContactUseCase useCase =
        new DeleteClientContactUseCase(clientRepository, clientContactRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    UUID contactId = UUID.randomUUID();

    Client client =
        new Client(
            clientId,
            accountId,
            "Atlas Studio",
            "active",
            "Notes",
            Instant.parse("2026-06-01T10:00:00Z"),
            null);
    ClientContact contact =
        new ClientContact(
            contactId,
            UUID.randomUUID(),
            "Julie",
            "Martin",
            null,
            null,
            null,
            false,
            Instant.parse("2026-06-01T10:00:00Z"),
            null);

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientContactRepository.findById(contactId)).thenReturn(Optional.of(contact));

    assertThrows(
        ClientNotFoundException.class,
        () -> useCase.execute(accountId, clientId, contactId));
  }
}
