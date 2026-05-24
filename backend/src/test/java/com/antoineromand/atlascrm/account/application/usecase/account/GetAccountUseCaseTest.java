package com.antoineromand.atlascrm.account.application.usecase.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.application.exceptions.AccountNotFoundException;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.account.domain.repository.IAccountRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAccountUseCaseTest {

  @Mock private IAccountRepository accountRepository;

  @Test
  void executeShouldReturnExistingProfile() {
    GetAccountUseCase useCase = new GetAccountUseCase(accountRepository);
    UUID credentialsId = UUID.randomUUID();
    Account account =
        new Account(
            UUID.randomUUID(),
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
            Instant.now(),
            null);

    when(accountRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.of(account));

    Account result = useCase.execute(credentialsId);

    assertEquals(account.getId(), result.getId());
    assertEquals(account.getCredentialsId(), result.getCredentialsId());
    assertEquals(account.getFirstName(), result.getFirstName());
    assertEquals(account.getLastName(), result.getLastName());
  }

  @Test
  void executeShouldThrowWhenProfileDoesNotExist() {
    GetAccountUseCase useCase = new GetAccountUseCase(accountRepository);
    UUID credentialsId = UUID.randomUUID();

    when(accountRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.empty());

    assertThrows(AccountNotFoundException.class, () -> useCase.execute(credentialsId));
  }
}
