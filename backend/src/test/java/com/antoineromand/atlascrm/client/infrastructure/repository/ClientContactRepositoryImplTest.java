package com.antoineromand.atlascrm.client.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.client.domain.ClientContact;
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
class ClientContactRepositoryImplTest {

  @Mock private ClientContactJpaRepository clientContactJpaRepository;
  @Mock private ClientJpaRepository clientJpaRepository;

  @Test
  void saveShouldDemoteExistingPrimaryBeforePersistingNewPrimary() {
    ClientContactRepositoryImpl repository =
        new ClientContactRepositoryImpl(clientContactJpaRepository, clientJpaRepository);
    UUID clientId = UUID.randomUUID();
    UUID existingContactId = UUID.randomUUID();
    UUID newContactId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-06-01T10:00:00Z");
    Instant updatedAt = Instant.parse("2026-06-01T11:00:00Z");

    AccountEntity account = org.mockito.Mockito.mock(AccountEntity.class);

    ClientEntity client = new ClientEntity(clientId, account, "Atlas Studio", "active", "Notes", createdAt, updatedAt);
    ClientContactEntity existingPrimary =
        new ClientContactEntity(
            existingContactId,
            client,
            "Julie",
            "Martin",
            "julie@example.com",
            "+33102030405",
            "CEO",
            true,
            createdAt,
            updatedAt);

    when(clientJpaRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(clientContactJpaRepository.findByClient_IdAndPrimaryTrue(clientId))
        .thenReturn(Optional.of(existingPrimary));
    when(clientContactJpaRepository.saveAndFlush(any(ClientContactEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(clientContactJpaRepository.save(any(ClientContactEntity.class)))
        .thenReturn(
            new ClientContactEntity(
                newContactId,
                client,
                "Paul",
                "Dupont",
                "paul@example.com",
                "+33987654321",
                "Sales",
                true,
                Instant.parse("2026-06-01T12:00:00Z"),
                null));

    UUID result =
        repository.save(
            new ClientContact(
                newContactId,
                clientId,
                "Paul",
                "Dupont",
                "paul@example.com",
                "+33987654321",
                "Sales",
                true,
                Instant.parse("2026-06-01T12:00:00Z"),
                null));

    ArgumentCaptor<ClientContactEntity> captor = ArgumentCaptor.forClass(ClientContactEntity.class);
    verify(clientContactJpaRepository).saveAndFlush(captor.capture());
    assertEquals(false, captor.getValue().isPrimary());
    assertEquals(newContactId, result);
  }
}
