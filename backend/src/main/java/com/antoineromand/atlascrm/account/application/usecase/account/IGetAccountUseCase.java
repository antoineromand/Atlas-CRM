package com.antoineromand.atlascrm.account.application.usecase.account;

import com.antoineromand.atlascrm.account.domain.Account;
import java.util.UUID;

public interface IGetAccountUseCase {
  Account execute(UUID credentialsId);
}
