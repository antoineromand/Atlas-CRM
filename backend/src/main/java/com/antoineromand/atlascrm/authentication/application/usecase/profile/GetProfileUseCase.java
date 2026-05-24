package com.antoineromand.atlascrm.authentication.application.usecase.profile;

import com.antoineromand.atlascrm.authentication.application.exceptions.ProfileNotFoundException;
import com.antoineromand.atlascrm.authentication.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.repository.IProfileRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetProfileUseCase implements IGetProfileUseCase {

  private final IProfileRepository profileRepository;

  public GetProfileUseCase(IProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  @Override
  public Profile execute(UUID credentialsId) {
    return this.profileRepository.findByCredentialsId(credentialsId)
        .orElseThrow(ProfileNotFoundException::new);
  }
}
