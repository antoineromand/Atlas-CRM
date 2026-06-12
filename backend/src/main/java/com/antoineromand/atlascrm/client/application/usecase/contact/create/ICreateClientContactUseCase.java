package com.antoineromand.atlascrm.client.application.usecase.contact.create;

import com.antoineromand.atlascrm.client.domain.ClientContact;
import java.util.UUID;

public interface ICreateClientContactUseCase {
  ClientContact execute(UUID accountId, UUID clientId, CreateClientContactCommand command);
}
