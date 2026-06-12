package com.antoineromand.atlascrm.client.application.usecase.list;

public interface IListClientUseCase {
  ClientPageResult execute(ListClientQuery query);
}
