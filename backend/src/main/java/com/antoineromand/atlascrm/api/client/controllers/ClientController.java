package com.antoineromand.atlascrm.api.client.controllers;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.client.dto.ClientPageResponseDto;
import com.antoineromand.atlascrm.api.client.dto.ClientResponseDto;
import com.antoineromand.atlascrm.client.application.usecase.list.IListClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.list.ListClientQuery;
import com.antoineromand.atlascrm.client.application.usecase.list.ClientPageResult;
import com.antoineromand.atlascrm.client.domain.Client;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

  private final IGetAccountUseCase getAccountUseCase;
  private final IListClientUseCase listClientUseCase;

  public ClientController(
      IGetAccountUseCase getAccountUseCase, IListClientUseCase listClientUseCase) {
    this.getAccountUseCase = getAccountUseCase;
    this.listClientUseCase = listClientUseCase;
  }

  @GetMapping
  public ResponseEntity<ClientPageResponseDto> listMyClients(
      Principal principal,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "6") int size) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    String normalizedSearch = this.normalizeSearch(search);
    String normalizedStatus = this.normalize(status);
    int normalizedPage = this.normalizePage(page);
    int normalizedSize = this.normalizeSize(size);

    ClientPageResult result =
        this.listClientUseCase.execute(
            new ListClientQuery(
                account.getId(), normalizedSearch, normalizedStatus, normalizedPage, normalizedSize));
    return ResponseEntity.ok(this.toPageResponse(result));
  }

  private UUID extractCredentialsId(Principal principal) {
    return UUID.fromString(principal.getName());
  }

  private String normalizeSearch(String search) {
    if (search == null) {
      return null;
    }

    String normalizedSearch = search.trim();

    if (normalizedSearch.isEmpty()) {
      return null;
    }

    if (normalizedSearch.length() < 3) {
      throw new IllegalArgumentException("search must contain at least 3 characters");
    }

    return normalizedSearch;
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private int normalizePage(int page) {
    if (page < 1) {
      throw new IllegalArgumentException("page must be greater than or equal to 1");
    }

    return page;
  }

  private int normalizeSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("size must be greater than or equal to 1");
    }

    if (size > 50) {
      throw new IllegalArgumentException("size must be lower than or equal to 50");
    }

    return size;
  }

  private ClientPageResponseDto toPageResponse(ClientPageResult result) {
    return new ClientPageResponseDto(
        result.items().stream().map(this::toResponse).toList(),
        result.page(),
        result.size(),
        result.totalElements(),
        result.totalPages(),
        result.hasNext(),
        result.hasPrevious());
  }

  private ClientResponseDto toResponse(Client client) {
    return new ClientResponseDto(
        client.getId(),
        client.getCompanyName(),
        client.getStatus(),
        client.getNotes(),
        client.getCreatedAt(),
        client.getUpdatedAt());
  }
}
