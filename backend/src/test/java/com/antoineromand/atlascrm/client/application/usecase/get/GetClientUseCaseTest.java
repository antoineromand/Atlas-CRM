package com.antoineromand.atlascrm.client.application.usecase.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.ClientTag;
import com.antoineromand.atlascrm.client.domain.repository.IClientActivityRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientContactRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientTagRepository;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetClientUseCaseTest {

  @Mock private IClientRepository clientRepository;
  @Mock private IClientContactRepository clientContactRepository;
  @Mock private IClientActivityRepository clientActivityRepository;
  @Mock private IClientTagRepository clientTagRepository;
  @Mock private IMissionRepository missionRepository;

  @Test
  void executeShouldReturnClientDetails() {
    GetClientUseCase useCase =
        new GetClientUseCase(
            clientRepository,
            clientContactRepository,
            clientActivityRepository,
            clientTagRepository,
            missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    Client client =
        new Client(
            clientId,
            accountId,
            "Atlas Studio",
            "Julie",
            "Martin",
            "active",
            "Important client",
            Instant.parse("2026-06-01T10:00:00Z"),
            null);

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.of(client));
    when(clientContactRepository.findAllByClientId(clientId))
        .thenReturn(
            List.of(
                new ClientContact(
                    UUID.randomUUID(),
                    clientId,
                    "Julie",
                    "Martin",
                    "julie.martin@example.com",
                    "+33 6 10 20 30 40",
                    "CEO",
                    true,
                    Instant.parse("2026-06-01T10:00:00Z"),
                    null)));
    when(clientActivityRepository.findAllByClientId(clientId))
        .thenReturn(
            List.of(
                new ClientActivity(
                    UUID.randomUUID(),
                    clientId,
                    "call",
                    "Kickoff call",
                    "Initial call",
                    Instant.parse("2026-06-01T09:00:00Z"),
                    Instant.parse("2026-06-01T09:00:00Z"),
                    null)));
    when(clientTagRepository.findAllByClientId(clientId))
        .thenReturn(
            List.of(
                new ClientTag(
                    UUID.randomUUID(),
                    accountId,
                    "VIP",
                    "#d97706",
                    Instant.parse("2026-06-01T08:00:00Z"),
                    null)));
    when(missionRepository.findAllByClientId(clientId))
        .thenReturn(
            List.of(
                new Mission(
                    UUID.randomUUID(),
                    accountId,
                    clientId,
                    "Website refresh",
                    "Lead designer",
                    "Design revamp",
                    "in_progress",
                    "high",
                    java.time.LocalDate.parse("2026-06-01"),
                    java.time.LocalDate.parse("2026-07-01"),
                    Instant.parse("2026-06-01T07:00:00Z"),
                    null)));

    ClientDetailResult result = useCase.execute(accountId, clientId);

    assertEquals(clientId, result.client().getId());
    assertEquals(1, result.contacts().size());
    assertEquals(1, result.activities().size());
    assertEquals(1, result.tags().size());
    assertEquals(1, result.missions().size());
  }

  @Test
  void executeShouldThrowWhenClientDoesNotExist() {
    GetClientUseCase useCase =
        new GetClientUseCase(
            clientRepository,
            clientContactRepository,
            clientActivityRepository,
            clientTagRepository,
            missionRepository);
    UUID accountId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    when(clientRepository.findByIdAndAccountId(clientId, accountId)).thenReturn(Optional.empty());

    assertThrows(
        ClientNotFoundException.class, () -> useCase.execute(accountId, clientId));
  }
}
