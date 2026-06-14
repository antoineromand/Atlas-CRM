package com.antoineromand.atlascrm.mission.application.usecase.list;

public interface IListMissionUseCase {
  MissionPageResult execute(ListMissionQuery query);
}
