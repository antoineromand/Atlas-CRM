package com.antoineromand.atlascrm.client.application.usecase.create;

import java.util.UUID;

public interface ICreateClientUseCase {
  UUID execute(CreateClientCommand command);
}
