package com.antoineromand.atlascrm.client.application.usecase.delete;

import java.util.UUID;

public interface IDeleteClientUseCase {
  void execute(UUID accountId, UUID clientId);
}
