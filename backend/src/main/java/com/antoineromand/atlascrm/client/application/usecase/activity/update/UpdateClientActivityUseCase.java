package com.antoineromand.atlascrm.client.application.usecase.activity.update;

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
public class UpdateClientActivityUseCase implements IUpdateClientActivityUseCase {

  private static final Set<String> ALLOWED_ACTIVITY_TYPES =
      Set.of("call", "email", "meeting", "note", "task", "follow_up", "status_change");

  private final IClientRepository clientRepository;
  private final IClientActivityRepository clientActivityRepository;

  public UpdateClientActivityUseCase(
      IClientRepository clientRepository, IClientActivityRepository clientActivityRepository) {
    this.clientRepository = clientRepository;
    this.clientActivityRepository = clientActivityRepository;
  }

  @Override
  public ClientActivity execute(
      UUID accountId, UUID clientId, UUID activityId, UpdateClientActivityCommand command) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    if (clientId == null) {
      throw new IllegalArgumentException("clientId must not be null");
    }

    if (activityId == null) {
      throw new IllegalArgumentException("activityId must not be null");
    }

    if (command == null) {
      throw new IllegalArgumentException("command must not be null");
    }

    Client existingClient =
        this.clientRepository
            .findByIdAndAccountId(clientId, accountId)
            .orElseThrow(
                () -> new ClientNotFoundException("CLIENT_NOT_FOUND", "The client does not exist."));

    ClientActivity existingActivity =
        this.clientActivityRepository
            .findById(activityId)
            .orElseThrow(
                () ->
                    new ClientNotFoundException(
                        "CLIENT_ACTIVITY_NOT_FOUND", "The activity does not exist."));

    if (!existingClient.getId().equals(existingActivity.getClientId())) {
      throw new ClientNotFoundException("CLIENT_ACTIVITY_NOT_FOUND", "The activity does not exist.");
    }

    ClientActivity updated =
        new ClientActivity(
            existingActivity.getId(),
            existingActivity.getClientId(),
            this.resolveActivityType(command.activityType(), existingActivity.getActivityType()),
            this.resolve(command.title(), existingActivity.getTitle(), "title"),
            this.resolveNullable(command.description(), existingActivity.getDescription()),
            this.resolve(command.occurredAt(), existingActivity.getOccurredAt(), "occurredAt"),
            existingActivity.getCreatedAt(),
            Instant.now());

    this.clientActivityRepository.save(updated);
    return this.clientActivityRepository
        .findById(activityId)
        .orElseThrow(
            () ->
                new ClientNotFoundException(
                    "CLIENT_ACTIVITY_NOT_FOUND", "The activity does not exist."));
  }

  private String resolve(String value, String currentValue, String fieldName) {
    String normalized = this.normalize(value);
    if (normalized == null) {
      if (currentValue == null || currentValue.isBlank()) {
        throw new IllegalArgumentException(fieldName + " must not be blank");
      }
      return currentValue;
    }
    return normalized;
  }

  private String resolveActivityType(String value, String currentValue) {
    if (value == null) {
      return this.requireActivityType(currentValue);
    }

    return this.requireActivityType(value);
  }

  private Instant resolve(Instant value, Instant currentValue, String fieldName) {
    if (value == null) {
      if (currentValue == null) {
        throw new IllegalArgumentException(fieldName + " must not be null");
      }
      return currentValue;
    }
    return value;
  }

  private String resolveNullable(String value, String currentValue) {
    if (value == null) {
      return currentValue;
    }

    return this.normalize(value);
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String requireActivityType(String value) {
    String normalized = this.normalize(value);
    if (normalized == null) {
      throw new IllegalArgumentException("activityType must not be blank");
    }

    if (!ALLOWED_ACTIVITY_TYPES.contains(normalized)) {
      throw new IllegalArgumentException("activityType must be one of: " + String.join(", ", ALLOWED_ACTIVITY_TYPES));
    }

    return normalized;
  }
}
