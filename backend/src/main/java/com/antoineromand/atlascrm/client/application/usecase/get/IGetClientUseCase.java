package com.antoineromand.atlascrm.client.application.usecase.get;

import java.util.UUID;

public interface IGetClientUseCase {
  ClientDetailResult execute(UUID accountId, UUID clientId);
}
