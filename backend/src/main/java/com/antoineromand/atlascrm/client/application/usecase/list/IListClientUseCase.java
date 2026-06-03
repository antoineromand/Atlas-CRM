package com.antoineromand.atlascrm.client.application.usecase.list;

import com.antoineromand.atlascrm.client.domain.Client;
import java.util.List;
import java.util.UUID;

public interface IListClientUseCase {
  List<Client> execute(UUID accountId);

  List<Client> execute(UUID accountId, String search, String status);

  ClientPageResult execute(UUID accountId, String search, String status, int page, int size);
}
