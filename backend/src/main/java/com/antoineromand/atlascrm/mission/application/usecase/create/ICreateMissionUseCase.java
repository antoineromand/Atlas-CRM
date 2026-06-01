package com.antoineromand.atlascrm.mission.application.usecase.create;

import java.util.UUID;

public interface ICreateMissionUseCase {
  UUID execute(CreateMissionCommand command);
}
