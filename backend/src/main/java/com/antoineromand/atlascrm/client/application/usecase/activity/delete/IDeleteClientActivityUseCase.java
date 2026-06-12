package com.antoineromand.atlascrm.client.application.usecase.activity.delete;

import java.util.UUID;

public interface IDeleteClientActivityUseCase {
  void execute(UUID accountId, UUID clientId, UUID activityId);
}
