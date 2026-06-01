package com.antoineromand.atlascrm.mission.application.usecase.list;

import com.antoineromand.atlascrm.mission.domain.Mission;
import java.util.List;
import java.util.UUID;

public interface IListMissionUseCase {
  List<Mission> execute(UUID accountId);
}
