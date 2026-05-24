package com.antoineromand.atlascrm.authentication.application.usecase.profile;

import com.antoineromand.atlascrm.authentication.domain.Profile;
import java.util.UUID;

public interface IUpdateProfileUseCase {
  Profile execute(UUID credentialsId, UpdateProfileCommand command);
}
