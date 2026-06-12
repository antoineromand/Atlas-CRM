package com.antoineromand.atlascrm.client.application.usecase.activity.create;

import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.repository.IClientActivityRepository;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateClientActivityUseCase implements ICreateClientActivityUseCase {

  private static final Set<String> ALLOWED_ACTIVITY_TYPES =
      Set.of("call", "email", "meeting", "note", "task", "follow_up", "status_change");

  private final IClientRepository clientRepository;
  private final IClientActivityRepository clientActivityRepository;

  public CreateClientActivityUseCase(
      IClientRepository clientRepository, IClientActivityRepository clientActivityRepository) {
    this.clientRepository = clientRepository;
    this.clientActivityRepository = clientActivityRepository;
  }

  @Override
  public ClientActivity execute(UUID accountId, UUID clientId, CreateClientActivityCommand command) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    if (command == null) {
      throw new IllegalArgumentException("command must not be null");
    }

    Client client =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));

    ClientActivity activity =
        new ClientActivity(
            null,
            client.getId(),
            this.requireActivityType(command.activityType()),
            this.requireValue(command.title(), "title"),
            this.normalize(command.description()),
            this.requireValue(command.occurredAt(), "occurredAt"),
            Instant.now(),
            null);

    UUID activityId = this.clientActivityRepository.save(activity);
    return this.clientActivityRepository
        .findById(activityId)
        .orElseThrow(
            () ->
                new ClientNotFoundException(
                    "CLIENT_ACTIVITY_NOT_FOUND", "The activity does not exist."));
  }

  private String requireValue(String value, String fieldName) {
    String normalized = this.normalize(value);
    if (normalized == null) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }

    return normalized;
  }

  private String requireActivityType(String value) {
    String normalized = this.requireValue(value, "activityType");
    if (!ALLOWED_ACTIVITY_TYPES.contains(normalized)) {
      throw new IllegalArgumentException("activityType must be one of: " + String.join(", ", ALLOWED_ACTIVITY_TYPES));
    }

    return normalized;
  }

  private Instant requireValue(Instant value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " must not be null");
    }

    return value;
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
