package com.antoineromand.atlascrm.client.application.usecase.activity.create;

import com.antoineromand.atlascrm.client.domain.ClientActivity;
import java.util.UUID;

public interface ICreateClientActivityUseCase {
  ClientActivity execute(UUID accountId, UUID clientId, CreateClientActivityCommand command);
}
