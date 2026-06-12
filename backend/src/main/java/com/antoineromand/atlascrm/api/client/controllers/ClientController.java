package com.antoineromand.atlascrm.api.client.controllers;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.client.dto.CreateClientRequestDto;
import com.antoineromand.atlascrm.api.client.dto.CreateClientResponseDto;
import com.antoineromand.atlascrm.api.client.dto.CreateClientContactRequestDto;
import com.antoineromand.atlascrm.api.client.dto.ClientActivityResponseDto;
import com.antoineromand.atlascrm.api.client.dto.ClientContactResponseDto;
import com.antoineromand.atlascrm.api.client.dto.ClientDetailResponseDto;
import com.antoineromand.atlascrm.api.client.dto.ClientPageResponseDto;
import com.antoineromand.atlascrm.api.client.dto.ClientResponseDto;
import com.antoineromand.atlascrm.api.client.dto.ClientTagResponseDto;
import com.antoineromand.atlascrm.api.client.dto.UpdateClientContactRequestDto;
import com.antoineromand.atlascrm.client.application.usecase.create.CreateClientCommand;
import com.antoineromand.atlascrm.client.application.usecase.create.ICreateClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.delete.IDeleteClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.get.ClientDetailResult;
import com.antoineromand.atlascrm.client.application.usecase.get.IGetClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.list.IListClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.list.ListClientQuery;
import com.antoineromand.atlascrm.client.application.usecase.list.ClientPageResult;
import com.antoineromand.atlascrm.client.application.usecase.contact.create.ICreateClientContactUseCase;
import com.antoineromand.atlascrm.client.application.usecase.contact.create.CreateClientContactCommand;
import com.antoineromand.atlascrm.client.application.usecase.contact.delete.IDeleteClientContactUseCase;
import com.antoineromand.atlascrm.client.application.usecase.contact.update.IUpdateClientContactUseCase;
import com.antoineromand.atlascrm.client.application.usecase.contact.update.UpdateClientContactCommand;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.ClientTag;
import com.antoineromand.atlascrm.client.application.usecase.update.IUpdateClientUseCase;
import com.antoineromand.atlascrm.client.application.usecase.update.UpdateClientCommand;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

  private final ICreateClientUseCase createClientUseCase;
  private final IGetAccountUseCase getAccountUseCase;
  private final IGetClientUseCase getClientUseCase;
  private final IUpdateClientUseCase updateClientUseCase;
  private final IDeleteClientUseCase deleteClientUseCase;
  private final ICreateClientContactUseCase createClientContactUseCase;
  private final IUpdateClientContactUseCase updateClientContactUseCase;
  private final IDeleteClientContactUseCase deleteClientContactUseCase;
  private final IListClientUseCase listClientUseCase;

  public ClientController(
      ICreateClientUseCase createClientUseCase,
      IGetAccountUseCase getAccountUseCase,
      IGetClientUseCase getClientUseCase,
      IUpdateClientUseCase updateClientUseCase,
      IDeleteClientUseCase deleteClientUseCase,
      ICreateClientContactUseCase createClientContactUseCase,
      IUpdateClientContactUseCase updateClientContactUseCase,
      IDeleteClientContactUseCase deleteClientContactUseCase,
      IListClientUseCase listClientUseCase) {
    this.createClientUseCase = createClientUseCase;
    this.getAccountUseCase = getAccountUseCase;
    this.getClientUseCase = getClientUseCase;
    this.updateClientUseCase = updateClientUseCase;
    this.deleteClientUseCase = deleteClientUseCase;
    this.createClientContactUseCase = createClientContactUseCase;
    this.updateClientContactUseCase = updateClientContactUseCase;
    this.deleteClientContactUseCase = deleteClientContactUseCase;
    this.listClientUseCase = listClientUseCase;
  }

  @PostMapping
  public ResponseEntity<CreateClientResponseDto> createClient(
      Principal principal, @Valid @RequestBody CreateClientRequestDto dto) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    UUID clientId =
        this.createClientUseCase.execute(
            new CreateClientCommand(account.getId(), dto.companyName(), dto.status(), dto.notes()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new CreateClientResponseDto(clientId, "Client created successfully."));
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

  @GetMapping("/{clientId}")
  public ResponseEntity<ClientDetailResponseDto> getMyClientById(
      Principal principal, @PathVariable UUID clientId) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    ClientDetailResult result = this.getClientUseCase.execute(account.getId(), clientId);
    return ResponseEntity.ok(this.toDetailResponse(result));
  }

  @PatchMapping("/{clientId}")
  public ResponseEntity<ClientResponseDto> updateClient(
      Principal principal, @PathVariable UUID clientId, @RequestBody Map<String, Object> body) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    Client updated =
        this.updateClientUseCase.execute(
            account.getId(),
            clientId,
            new UpdateClientCommand(
                this.patchString(body, "companyName", 200),
                this.patchString(body, "status", 32),
                this.patchString(body, "notes", Integer.MAX_VALUE)));
    return ResponseEntity.ok(this.toResponse(updated));
  }

  @PostMapping("/{clientId}/contacts")
  public ResponseEntity<ClientContactResponseDto> createClientContact(
      Principal principal,
      @PathVariable UUID clientId,
      @Valid @RequestBody CreateClientContactRequestDto dto) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    ClientContact contact =
        this.createClientContactUseCase.execute(
            account.getId(),
            clientId,
            new CreateClientContactCommand(
                dto.firstName(), dto.lastName(), dto.email(), dto.phone(), dto.jobTitle(), dto.primary()));
    return ResponseEntity.status(HttpStatus.CREATED).body(this.toResponse(contact));
  }

  @PatchMapping("/{clientId}/contacts/{contactId}")
  public ResponseEntity<ClientContactResponseDto> updateClientContact(
      Principal principal,
      @PathVariable UUID clientId,
      @PathVariable UUID contactId,
      @Valid @RequestBody UpdateClientContactRequestDto dto) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    ClientContact contact =
        this.updateClientContactUseCase.execute(
            account.getId(),
            clientId,
            contactId,
            new UpdateClientContactCommand(
                dto.firstName(), dto.lastName(), dto.email(), dto.phone(), dto.jobTitle(), dto.primary()));
    return ResponseEntity.ok(this.toResponse(contact));
  }

  @DeleteMapping("/{clientId}/contacts/{contactId}")
  public ResponseEntity<Void> deleteClientContact(
      Principal principal, @PathVariable UUID clientId, @PathVariable UUID contactId) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    this.deleteClientContactUseCase.execute(account.getId(), clientId, contactId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{clientId}")
  public ResponseEntity<Void> deleteClient(Principal principal, @PathVariable UUID clientId) {
    Account account = this.getAccountUseCase.execute(this.extractCredentialsId(principal));
    this.deleteClientUseCase.execute(account.getId(), clientId);
    return ResponseEntity.noContent().build();
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

  private PatchValue<String> patchString(Map<String, Object> body, String fieldName, int maxLength) {
    if (!body.containsKey(fieldName)) {
      return PatchValue.absent();
    }

    Object rawValue = body.get(fieldName);
    if (rawValue == null) {
      return PatchValue.of(null);
    }

    if (!(rawValue instanceof String value)) {
      throw new IllegalArgumentException(fieldName + " must be a string or null");
    }

    if (value.length() > maxLength) {
      throw new IllegalArgumentException(fieldName + " exceeds max length of " + maxLength);
    }

    return PatchValue.of(value);
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
        client.getPrimaryContactFirstName(),
        client.getPrimaryContactLastName(),
        client.getStatus(),
        client.getNotes(),
        client.getCreatedAt(),
        client.getUpdatedAt());
  }

  private ClientDetailResponseDto toDetailResponse(ClientDetailResult result) {
    return new ClientDetailResponseDto(
        this.toResponse(result.client()),
        result.contacts().stream().map(this::toResponse).toList(),
        result.activities().stream().map(this::toResponse).toList(),
        result.tags().stream().map(this::toResponse).toList());
  }

  private ClientContactResponseDto toResponse(ClientContact contact) {
    return new ClientContactResponseDto(
        contact.getId(),
        contact.getFirstName(),
        contact.getLastName(),
        contact.getEmail(),
        contact.getPhone(),
        contact.getJobTitle(),
        contact.isPrimary(),
        contact.getCreatedAt(),
        contact.getUpdatedAt());
  }

  private ClientActivityResponseDto toResponse(ClientActivity activity) {
    return new ClientActivityResponseDto(
        activity.getId(),
        activity.getActivityType(),
        activity.getTitle(),
        activity.getDescription(),
        activity.getOccurredAt(),
        activity.getCreatedAt(),
        activity.getUpdatedAt());
  }

  private ClientTagResponseDto toResponse(ClientTag tag) {
    return new ClientTagResponseDto(
        tag.getId(),
        tag.getName(),
        tag.getColor(),
        tag.getCreatedAt(),
        tag.getUpdatedAt());
  }
}
