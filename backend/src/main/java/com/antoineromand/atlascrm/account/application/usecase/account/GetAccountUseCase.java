package com.antoineromand.atlascrm.account.application.usecase.account;

import com.antoineromand.atlascrm.account.application.exceptions.AccountNotFoundException;
import com.antoineromand.atlascrm.account.domain.Profile;
import com.antoineromand.atlascrm.account.domain.repository.IProfileRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetAccountUseCase implements IGetAccountUseCase {

  private final IProfileRepository profileRepository;

  public GetAccountUseCase(IProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  @Override
  public Profile execute(UUID credentialsId) {
    return this.profileRepository.findByCredentialsId(credentialsId)
        .orElseThrow(AccountNotFoundException::new);
  }
}
