package com.antoineromand.atlascrm.client.application.usecase.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ListClientUseCaseTest {

  @Mock private IClientRepository clientRepository;

  @Test
  void executeShouldReturnAllClientsForAccount() {
    ListClientUseCase useCase = new ListClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    var clients =
        java.util.List.of(
            new Client(
                UUID.randomUUID(),
                accountId,
                "Atlas Studio",
                "active",
                null,
                Instant.now(),
                null));

    when(clientRepository.findAllByAccountId(
            accountId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))))
        .thenReturn(new PageImpl<>(clients, PageRequest.of(0, 10), 1));

    ClientPageResult result = useCase.execute(new ListClientQuery(accountId, null, null, 1, 10));

    assertEquals(1, result.items().size());
    assertEquals("Atlas Studio", result.items().get(0).getCompanyName());
  }

  @Test
  void executeShouldFilterClientsBySearchAndStatus() {
    ListClientUseCase useCase = new ListClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    var clients =
        java.util.List.of(
            new Client(
                UUID.randomUUID(),
                accountId,
                "Atlas Studio",
                "active",
                null,
                Instant.now(),
                null));

    when(clientRepository.findAllByAccountIdAndSearch(
            accountId, "atlas", "active", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))))
        .thenReturn(new PageImpl<>(clients, PageRequest.of(0, 10), 1));

    ClientPageResult result = useCase.execute(new ListClientQuery(accountId, " atlas ", " active ", 1, 10));

    assertEquals(1, result.items().size());
    assertEquals("Atlas Studio", result.items().get(0).getCompanyName());
    verify(clientRepository)
        .findAllByAccountIdAndSearch(
            accountId, "atlas", "active", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  @Test
  void executeShouldReturnPagedResults() {
    ListClientUseCase useCase = new ListClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    List<Client> clients =
        List.of(
            new Client(
                UUID.randomUUID(),
                accountId,
                "Atlas Studio",
                "active",
                null,
                Instant.now(),
                null));

    when(clientRepository.findAllByAccountId(
            accountId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))))
        .thenReturn(new PageImpl<>(clients, PageRequest.of(0, 10), 1));

    ClientPageResult result = useCase.execute(new ListClientQuery(accountId, null, null, 1, 10));

    assertEquals(1, result.items().size());
    assertEquals(1, result.page());
    assertEquals(10, result.size());
    assertEquals(1L, result.totalElements());
    assertEquals(1, result.totalPages());
    assertEquals(false, result.hasNext());
    assertEquals(false, result.hasPrevious());
  }

  @Test
  void executeShouldReturnPagedFilteredResults() {
    ListClientUseCase useCase = new ListClientUseCase(clientRepository);
    UUID accountId = UUID.randomUUID();
    List<Client> clients =
        List.of(
            new Client(
                UUID.randomUUID(),
                accountId,
                "Atlas Studio",
                "active",
                null,
                Instant.now(),
                null));

    when(clientRepository.findAllByAccountIdAndSearch(
            accountId, "atlas", "active", PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"))))
        .thenReturn(new PageImpl<>(clients, PageRequest.of(0, 6), 1));

    ClientPageResult result = useCase.execute(new ListClientQuery(accountId, " atlas ", " active ", 1, 6));

    assertEquals(1, result.items().size());
    assertEquals("Atlas Studio", result.items().get(0).getCompanyName());
    verify(clientRepository)
        .findAllByAccountIdAndSearch(
            accountId, "atlas", "active", PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt")));
  }
}
