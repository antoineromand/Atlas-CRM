package com.antoineromand.atlascrm.authentication.application.usecase.profile;

import com.antoineromand.atlascrm.authentication.application.exceptions.ProfileNotFoundException;
import com.antoineromand.atlascrm.authentication.domain.Profile;
import com.antoineromand.atlascrm.authentication.domain.repository.IProfileRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateProfileUseCase implements IUpdateProfileUseCase {

  private final IProfileRepository profileRepository;

  public UpdateProfileUseCase(IProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  @Override
  public Profile execute(UUID credentialsId, UpdateProfileCommand command) {
    Profile existing =
        this.profileRepository.findByCredentialsId(credentialsId)
            .orElseThrow(ProfileNotFoundException::new);

    Profile updated =
        new Profile(
            existing.getId(),
            existing.getCredentialsId(),
            command.firstName() != null ? command.firstName() : existing.getFirstName(),
            command.lastName() != null ? command.lastName() : existing.getLastName(),
            command.companyName() != null ? command.companyName() : existing.getCompanyName(),
            command.siretNumber() != null ? command.siretNumber() : existing.getSiretNumber(),
            command.vatNumber() != null ? command.vatNumber() : existing.getVatNumber(),
            command.billingEmail() != null ? command.billingEmail() : existing.getBillingEmail(),
            command.billingAddressLine1() != null
                ? command.billingAddressLine1()
                : existing.getBillingAddressLine1(),
            command.billingAddressLine2() != null
                ? command.billingAddressLine2()
                : existing.getBillingAddressLine2(),
            command.billingPostalCode() != null
                ? command.billingPostalCode()
                : existing.getBillingPostalCode(),
            command.billingCity() != null ? command.billingCity() : existing.getBillingCity(),
            command.billingCountry() != null ? command.billingCountry() : existing.getBillingCountry(),
            existing.getCreatedAt(),
            existing.getUpdatedAt());

    this.profileRepository.save(updated);
    return this.profileRepository.findByCredentialsId(credentialsId)
        .orElseThrow(ProfileNotFoundException::new);
  }
}
