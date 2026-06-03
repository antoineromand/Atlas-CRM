package com.antoineromand.atlascrm.api.client.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.client.dto.ClientPageResponseDto;
import com.antoineromand.atlascrm.client.application.usecase.list.ClientPageResult;
import com.antoineromand.atlascrm.client.application.usecase.list.IListClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.list.ListClientQuery;
import com.antoineromand.atlascrm.client.domain.Client;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
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

  @Mock private IGetAccountUseCase getAccountUseCase;
  @Mock private IListClientUseCase listClientUseCase;

  @Test
  void listMyClientsShouldResolveCurrentAccountAndReturnPagedResponse() {
    ClientController controller = new ClientController(getAccountUseCase, listClientUseCase);
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
  }

  @Test
  void listMyClientsShouldForwardNormalizedQueryToUseCase() {
    ClientController controller = new ClientController(getAccountUseCase, listClientUseCase);
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
    org.mockito.Mockito.verify(listClientUseCase).execute(captor.capture());
    assertEquals(accountId, captor.getValue().accountId());
    assertEquals(null, captor.getValue().search());
    assertEquals(null, captor.getValue().status());
  }
}
