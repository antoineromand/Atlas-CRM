package com.antoineromand.atlascrm.client.application.usecase.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import com.antoineromand.atlascrm.client.application.exceptions.ClientCreationException;
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
class CreateClientUseCaseTest {

  @Mock private IClientRepository clientRepository;
  @Mock private IAccountRepository accountRepository;

  @Test
  void executeShouldPersistClientAndApplyDefaultsWhenNeeded() {
    CreateClientUseCase useCase = new CreateClientUseCase(clientRepository, accountRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    when(accountRepository.findById(accountId))
        .thenReturn(
            Optional.of(
                new Account(
                    accountId,
                    UUID.randomUUID(),
                    null,
                    null,
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
                    null)));
    when(clientRepository.save(any(Client.class))).thenReturn(clientId);

    UUID result =
        useCase.execute(
            new CreateClientCommand(
                accountId,
                "Atlas Studio",
                null,
                "  Important client  "));

    ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
    verify(clientRepository).save(captor.capture());
    Client saved = captor.getValue();

    assertEquals(clientId, result);
    assertEquals(accountId, saved.getAccountId());
    assertEquals("Atlas Studio", saved.getCompanyName());
    assertEquals("prospect", saved.getStatus());
    assertEquals("Important client", saved.getNotes());
  }

  @Test
  void executeShouldThrowWhenAccountDoesNotExist() {
    CreateClientUseCase useCase = new CreateClientUseCase(clientRepository, accountRepository);
    UUID accountId = UUID.randomUUID();

    when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

    ClientCreationException exception =
        assertThrows(
            ClientCreationException.class,
            () ->
                useCase.execute(
                    new CreateClientCommand(accountId, "Atlas Studio", null, null)));

    assertEquals("ACCOUNT_NOT_FOUND", exception.getCode());
    verify(clientRepository, never()).save(any());
  }
}
