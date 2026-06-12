package com.antoineromand.atlascrm.client.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.account.infrastructure.repository.AccountJpaRepository;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientContactEntity;
import com.antoineromand.atlascrm.client.infrastructure.model.ClientEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientRepositoryImplTest {

  @Mock private ClientJpaRepository clientJpaRepository;
  @Mock private AccountJpaRepository accountJpaRepository;

  @Test
  void saveShouldUpdateExistingClientWithoutDroppingContacts() {
    ClientRepositoryImpl repository = new ClientRepositoryImpl(clientJpaRepository, accountJpaRepository);

    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Instant updatedAt = Instant.parse("2026-06-01T11:00:00Z");

    AccountEntity account = org.mockito.Mockito.mock(AccountEntity.class);
    when(account.getId()).thenReturn(accountId);
    when(accountJpaRepository.findById(accountId)).thenReturn(Optional.of(account));

    ClientEntity existing = new ClientEntity(clientId, account, "Old company", "prospect", "Old notes", createdAt, updatedAt);
    ClientContactEntity contact =
        new ClientContactEntity(
            UUID.randomUUID(),
            existing,
            "Julie",
            "Martin",
            "julie@example.com",
            "+33102030405",
            "Manager",
            true,
            createdAt,
            updatedAt);
    existing.getContacts().add(contact);

    when(clientJpaRepository.findByIdAndAccount_Id(clientId, accountId)).thenReturn(Optional.of(existing));
    when(clientJpaRepository.save(any(ClientEntity.class))).thenReturn(existing);

    UUID result =
        repository.save(
            new Client(
                clientId,
                accountId,
                "New company",
                "active",
                "New notes",
                createdAt,
                updatedAt));

    ArgumentCaptor<ClientEntity> captor = ArgumentCaptor.forClass(ClientEntity.class);
    verify(clientJpaRepository).save(captor.capture());

    ClientEntity persisted = captor.getValue();
    assertEquals(clientId, result);
    assertEquals("New company", persisted.getCompanyName());
    assertEquals("active", persisted.getStatus());
    assertEquals("New notes", persisted.getNotes());
    assertEquals(1, persisted.getContacts().size());
    assertEquals("Julie", persisted.getContacts().iterator().next().getFirstName());
  }
}
