package com.antoineromand.atlascrm.account.application.usecase.account;

import com.antoineromand.atlascrm.account.domain.Profile;
import java.util.UUID;

public interface IGetAccountUseCase {
  Profile execute(UUID credentialsId);
}
