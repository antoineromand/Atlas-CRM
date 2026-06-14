package com.antoineromand.atlascrm.client.application.usecase.contact.delete;

import java.util.UUID;

public interface IDeleteClientContactUseCase {
  void execute(UUID accountId, UUID clientId, UUID contactId);
}
