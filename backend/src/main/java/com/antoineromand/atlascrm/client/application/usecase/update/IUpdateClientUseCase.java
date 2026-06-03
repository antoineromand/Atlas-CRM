package com.antoineromand.atlascrm.client.application.usecase.update;

import com.antoineromand.atlascrm.client.domain.Client;
import java.util.UUID;

public interface IUpdateClientUseCase {
  Client execute(UUID accountId, UUID clientId, UpdateClientCommand command);
}
