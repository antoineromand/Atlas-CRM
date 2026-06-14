package com.antoineromand.atlascrm.client.application.usecase.activity.update;

import com.antoineromand.atlascrm.client.domain.ClientActivity;
import java.util.UUID;

public interface IUpdateClientActivityUseCase {
  ClientActivity execute(
      UUID accountId, UUID clientId, UUID activityId, UpdateClientActivityCommand command);
}
