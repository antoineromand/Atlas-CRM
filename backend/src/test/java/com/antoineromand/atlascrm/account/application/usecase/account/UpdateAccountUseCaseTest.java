package com.antoineromand.atlascrm.account.application.usecase.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.exceptions.AccountNotFoundException;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateAccountUseCaseTest {

  @Mock private IAccountRepository accountRepository;

  @Test
  void executeShouldMergeFieldsAndReturnUpdatedAccount() {
    UpdateAccountUseCase useCase = new UpdateAccountUseCase(accountRepository);
    UUID credentialsId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Instant createdAt = Instant.now();
    Account existing =
        new Account(
            accountId,
            credentialsId,
            "John",
            "Doe",
            "JD Consulting",
            "12345678901234",
            "FR12345678901",
            "billing@example.com",
            "10 rue de Paris",
            null,
            "75000",
            "Paris",
            "France",
            createdAt,
            null);
    Account updated =
        new Account(
            accountId,
            credentialsId,
            "Jane",
            "Doe",
            "JD Consulting",
            null,
            "FR12345678901",
            "billing@example.com",
            "20 rue de Lyon",
            null,
            "69000",
            "Lyon",
            "France",
            createdAt,
            Instant.now());

    when(accountRepository.findByCredentialsId(credentialsId))
        .thenReturn(Optional.of(existing), Optional.of(updated));
    when(accountRepository.save(any(Account.class))).thenReturn(accountId);

    Account result =
        useCase.execute(
            credentialsId,
            new UpdateAccountCommand(
                PatchValue.of("Jane"),
                PatchValue.absent(),
                PatchValue.absent(),
                PatchValue.of(null),
                PatchValue.absent(),
                PatchValue.absent(),
                PatchValue.of("20 rue de Lyon"),
                PatchValue.absent(),
                PatchValue.of("69000"),
                PatchValue.of("Lyon"),
                PatchValue.absent()));

    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).save(captor.capture());
    Account saved = captor.getValue();

    assertEquals(accountId, result.getId());
    assertEquals("Jane", saved.getFirstName());
    assertEquals("Doe", saved.getLastName());
    assertEquals(null, saved.getSiretNumber());
    assertEquals("20 rue de Lyon", saved.getBillingAddressLine1());
    assertEquals("69000", saved.getBillingPostalCode());
    assertEquals("Lyon", saved.getBillingCity());
    assertNotNull(result.getUpdatedAt());
  }

  @Test
  void executeShouldThrowWhenAccountDoesNotExist() {
    UpdateAccountUseCase useCase = new UpdateAccountUseCase(accountRepository);
    UUID credentialsId = UUID.randomUUID();

    when(accountRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.empty());

    assertThrows(
        AccountNotFoundException.class,
        () ->
            useCase.execute(
                credentialsId,
                new UpdateAccountCommand(
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent(),
                    PatchValue.absent())));
  }
}
