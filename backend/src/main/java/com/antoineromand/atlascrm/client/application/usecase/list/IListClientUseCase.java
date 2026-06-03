package com.antoineromand.atlascrm.client.application.usecase.list;

import com.antoineromand.atlascrm.client.domain.Client;

public interface IListClientUseCase {
  ClientPageResult execute(ListClientQuery query);
}
