package com.antoineromand.atlascrm.client.application.usecase.contact.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateClientContactUseCaseTest {

  @Mock private IClientRepository clientRepository;
  @Mock private IClientContactRepository clientContactRepository;

  @Test
  void executeShouldMergeFieldsAndReturnContact() {
    UpdateClientContactUseCase useCase =
        new UpdateClientContactUseCase(clientRepository, clientContactRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    UUID contactId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Instant updatedAt = Instant.parse("2026-06-01T11:00:00Z");

    Client client =
        new Client(
            clientId,
            accountId,
            "Atlas Studio",
            "active",
            "Notes",
            createdAt,
            null);
    ClientContact existing =
        new ClientContact(
            contactId,
            clientId,
            "Julie",
            "Martin",
            "julie@example.com",
            "+33102030405",
            "CEO",
            false,
            createdAt,
            updatedAt);

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientContactRepository.findById(contactId))
        .thenReturn(
            Optional.of(existing),
            Optional.of(
                new ClientContact(
                    contactId,
                    clientId,
                    "Julie",
                    "Dupont",
                    "julie.dupont@example.com",
                    "+33987654321",
                    "Managing Director",
                    false,
                    createdAt,
                    Instant.parse("2026-06-01T12:00:00Z"))));
    when(clientContactRepository.save(any(ClientContact.class))).thenReturn(contactId);

    ClientContact result =
        useCase.execute(
            accountId,
            clientId,
            contactId,
            new UpdateClientContactCommand(
                "Julie",
                "Dupont",
                "julie.dupont@example.com",
                "+33987654321",
                "Managing Director",
                true));

    ArgumentCaptor<ClientContact> captor = ArgumentCaptor.forClass(ClientContact.class);
    verify(clientContactRepository).save(captor.capture());

    assertEquals(contactId, result.getId());
    assertEquals("Dupont", captor.getValue().getLastName());
    assertEquals("julie.dupont@example.com", captor.getValue().getEmail());
    assertTrue(captor.getValue().isPrimary());
  }

  @Test
  void executeShouldThrowWhenContactDoesNotBelongToClient() {
    UpdateClientContactUseCase useCase =
        new UpdateClientContactUseCase(clientRepository, clientContactRepository);
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
        () ->
            useCase.execute(
                accountId,
                clientId,
                contactId,
                new UpdateClientContactCommand("Julie", "Martin", null, null, null, false)));
  }
}
