package com.antoineromand.atlascrm.account.application.usecase.account;

import com.antoineromand.atlascrm.account.domain.Profile;
import java.util.UUID;

public interface IUpdateAccountUseCase {
  Profile execute(UUID credentialsId, UpdateAccountCommand command);
}
