package com.antoineromand.atlascrm.client.application.usecase.contact.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
class CreateClientContactUseCaseTest {

  @Mock private IClientRepository clientRepository;
  @Mock private IClientContactRepository clientContactRepository;

  @Test
  void executeShouldCreateAndReturnContact() {
    CreateClientContactUseCase useCase =
        new CreateClientContactUseCase(clientRepository, clientContactRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    UUID contactId = UUID.randomUUID();
    Client client =
        new Client(
            clientId,
            accountId,
            "Atlas Studio",
            "prospect",
            "Notes",
            Instant.parse("2026-06-01T10:00:00Z"),
            null);

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientContactRepository.save(any(ClientContact.class))).thenReturn(contactId);
    when(clientContactRepository.findById(contactId))
        .thenReturn(
            Optional.of(
                new ClientContact(
                    contactId,
                    clientId,
                    "Julie",
                    "Martin",
                    "julie@example.com",
                    "+33102030405",
                    "CEO",
                    true,
                    Instant.parse("2026-06-01T11:00:00Z"),
                    null)));

    ClientContact result =
        useCase.execute(
            accountId,
            clientId,
            new CreateClientContactCommand(
                "Julie", "Martin", "julie@example.com", "+33102030405", "CEO", true));

    assertEquals(contactId, result.getId());
    assertEquals(clientId, result.getClientId());
    assertEquals("Julie", result.getFirstName());
  }

  @Test
  void executeShouldThrowWhenClientDoesNotExist() {
    CreateClientContactUseCase useCase =
        new CreateClientContactUseCase(clientRepository, clientContactRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.empty());

    assertThrows(
        ClientNotFoundException.class,
        () ->
            useCase.execute(
                accountId,
                clientId,
                new CreateClientContactCommand("Julie", "Martin", null, null, null, false)));
  }
}
