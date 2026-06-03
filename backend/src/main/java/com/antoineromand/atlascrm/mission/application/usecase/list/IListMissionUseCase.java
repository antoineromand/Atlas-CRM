package com.antoineromand.atlascrm.mission.application.usecase.list;

import com.antoineromand.atlascrm.mission.domain.Mission;

public interface IListMissionUseCase {
  MissionPageResult execute(ListMissionQuery query);
}
