package com.antoineromand.atlascrm.api.client.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.client.dto.ClientPageResponseDto;
import com.antoineromand.atlascrm.api.client.dto.ClientResponseDto;
import com.antoineromand.atlascrm.api.client.dto.CreateClientRequestDto;
import com.antoineromand.atlascrm.api.client.dto.CreateClientResponseDto;
import com.antoineromand.atlascrm.client.application.usecase.create.CreateClientCommand;
import com.antoineromand.atlascrm.client.application.usecase.create.ICreateClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.delete.IDeleteClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.list.ClientPageResult;
import com.antoineromand.atlascrm.client.application.usecase.list.IListClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.list.ListClientQuery;
import com.antoineromand.atlascrm.client.application.usecase.update.IUpdateClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.update.UpdateClientCommand;
import com.antoineromand.atlascrm.client.domain.Client;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

  @Mock private ICreateClientUseCase createClientUseCase;
  @Mock private IGetAccountUseCase getAccountUseCase;
  @Mock private IUpdateClientUseCase updateClientUseCase;
  @Mock private IDeleteClientUseCase deleteClientUseCase;
  @Mock private IListClientUseCase listClientUseCase;

  @Test
  void createClientShouldResolveCurrentAccountAndReturnCreatedResponse() {
    ClientController controller =
        new ClientController(
            createClientUseCase, getAccountUseCase, updateClientUseCase, deleteClientUseCase, listClientUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    Principal principal = () -> credentialsId.toString();

    when(getAccountUseCase.execute(credentialsId))
        .thenReturn(
            new Account(
                accountId,
                credentialsId,
                "John",
                "Doe",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null));
    when(createClientUseCase.execute(any(CreateClientCommand.class))).thenReturn(clientId);

    ResponseEntity<CreateClientResponseDto> response =
        controller.createClient(
            principal, new CreateClientRequestDto("Nova Studio", "prospect", "First contact"));

    ArgumentCaptor<CreateClientCommand> captor = ArgumentCaptor.forClass(CreateClientCommand.class);
    verify(createClientUseCase).execute(captor.capture());

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(clientId, response.getBody().clientId());
    assertEquals("Client created successfully.", response.getBody().message());
    assertEquals(accountId, captor.getValue().accountId());
    assertEquals("Nova Studio", captor.getValue().companyName());
    assertEquals("prospect", captor.getValue().status());
    assertEquals("First contact", captor.getValue().notes());
  }

  @Test
  void listMyClientsShouldResolveCurrentAccountAndReturnPagedResponse() {
    ClientController controller =
        new ClientController(
            createClientUseCase, getAccountUseCase, updateClientUseCase, deleteClientUseCase, listClientUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Principal principal = () -> credentialsId.toString();

    when(getAccountUseCase.execute(credentialsId))
        .thenReturn(
            new Account(
                accountId,
                credentialsId,
                "John",
                "Doe",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null));

    when(listClientUseCase.execute(new ListClientQuery(accountId, "atlas", "active", 1, 6)))
        .thenReturn(
            new ClientPageResult(
                List.of(
                    new Client(
                        UUID.randomUUID(),
                        accountId,
                        "Atlas Studio",
                        "Julie",
                        "Martin",
                        "active",
                        "Important client",
                        Instant.parse("2026-06-01T10:00:00Z"),
                        null)),
                1,
                6,
                1,
                1,
                false,
                false));

    ResponseEntity<ClientPageResponseDto> response =
        controller.listMyClients(principal, " atlas ", " active ", 1, 6);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().items().size());
    assertEquals("Atlas Studio", response.getBody().items().get(0).companyName());
    assertEquals("Julie", response.getBody().items().get(0).primaryContactFirstName());
    assertEquals("Martin", response.getBody().items().get(0).primaryContactLastName());
  }

  @Test
  void listMyClientsShouldForwardNormalizedQueryToUseCase() {
    ClientController controller =
        new ClientController(
            createClientUseCase, getAccountUseCase, updateClientUseCase, deleteClientUseCase, listClientUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Principal principal = () -> credentialsId.toString();

    when(getAccountUseCase.execute(credentialsId))
        .thenReturn(
            new Account(
                accountId,
                credentialsId,
                "John",
                "Doe",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null));

    when(listClientUseCase.execute(new ListClientQuery(accountId, null, null, 1, 6)))
        .thenReturn(new ClientPageResult(List.of(), 1, 6, 0, 0, false, false));

    controller.listMyClients(principal, "   ", "   ", 1, 6);

    ArgumentCaptor<ListClientQuery> captor = ArgumentCaptor.forClass(ListClientQuery.class);
    verify(listClientUseCase).execute(captor.capture());
    assertEquals(accountId, captor.getValue().accountId());
    assertNull(captor.getValue().search());
    assertNull(captor.getValue().status());
  }

  @Test
  void updateClientShouldResolveCurrentAccountAndReturnUpdatedClient() {
    ClientController controller =
        new ClientController(
            createClientUseCase, getAccountUseCase, updateClientUseCase, deleteClientUseCase, listClientUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    Principal principal = () -> credentialsId.toString();

    when(getAccountUseCase.execute(credentialsId))
        .thenReturn(
            new Account(
                accountId,
                credentialsId,
                "John",
                "Doe",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null));
    when(updateClientUseCase.execute(
            accountId,
            clientId,
            new UpdateClientCommand(
                PatchValue.of("Updated Studio"),
                PatchValue.of("active"),
                PatchValue.of("Updated notes"))))
        .thenReturn(
            new Client(
                clientId,
                accountId,
                "Updated Studio",
                "Julie",
                "Martin",
                "active",
                "Updated notes",
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-01T11:00:00Z")));

    ResponseEntity<ClientResponseDto> response =
        controller.updateClient(
            principal,
            clientId,
            Map.of("companyName", "Updated Studio", "status", "active", "notes", "Updated notes"));

    ArgumentCaptor<UpdateClientCommand> captor = ArgumentCaptor.forClass(UpdateClientCommand.class);
    verify(updateClientUseCase).execute(org.mockito.ArgumentMatchers.eq(accountId), org.mockito.ArgumentMatchers.eq(clientId), captor.capture());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Updated Studio", response.getBody().companyName());
    assertEquals("Julie", response.getBody().primaryContactFirstName());
    assertEquals("Updated Studio", captor.getValue().companyName().value());
    assertEquals("active", captor.getValue().status().value());
    assertEquals("Updated notes", captor.getValue().notes().value());
  }

  @Test
  void deleteClientShouldResolveCurrentAccountAndReturnNoContent() {
    ClientController controller =
        new ClientController(
            createClientUseCase, getAccountUseCase, updateClientUseCase, deleteClientUseCase, listClientUseCase);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    Principal principal = () -> credentialsId.toString();

    when(getAccountUseCase.execute(credentialsId))
        .thenReturn(
            new Account(
                accountId,
                credentialsId,
                "John",
                "Doe",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null));

    ResponseEntity<Void> response = controller.deleteClient(principal, clientId);

    verify(deleteClientUseCase).execute(accountId, clientId);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());
  }
}
