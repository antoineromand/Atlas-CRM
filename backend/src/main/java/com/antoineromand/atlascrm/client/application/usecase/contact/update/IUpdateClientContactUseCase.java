package com.antoineromand.atlascrm.client.application.usecase.contact.update;

import com.antoineromand.atlascrm.client.domain.ClientContact;
import java.util.UUID;

public interface IUpdateClientContactUseCase {
  ClientContact execute(UUID accountId, UUID clientId, UUID contactId, UpdateClientContactCommand command);
}
