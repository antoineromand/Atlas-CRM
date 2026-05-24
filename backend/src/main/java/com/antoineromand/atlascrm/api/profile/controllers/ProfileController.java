package com.antoineromand.atlascrm.api.profile.controllers;

import com.antoineromand.atlascrm.api.profile.dto.ProfileResponseDto;
import com.antoineromand.atlascrm.api.profile.dto.UpdateProfileRequestDto;
import com.antoineromand.atlascrm.authentication.application.usecase.profile.IGetProfileUseCase;
import com.antoineromand.atlascrm.authentication.application.usecase.profile.IUpdateProfileUseCase;
import com.antoineromand.atlascrm.authentication.application.usecase.profile.UpdateProfileCommand;
import com.antoineromand.atlascrm.authentication.domain.Profile;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

  private final IGetProfileUseCase getProfileUseCase;
  private final IUpdateProfileUseCase updateProfileUseCase;

  public ProfileController(
      IGetProfileUseCase getProfileUseCase, IUpdateProfileUseCase updateProfileUseCase) {
    this.getProfileUseCase = getProfileUseCase;
    this.updateProfileUseCase = updateProfileUseCase;
  }

  @GetMapping("/me")
  public ResponseEntity<ProfileResponseDto> getMyProfile(Principal principal) {
    return ResponseEntity.ok(this.toResponse(this.getProfileUseCase.execute(this.extractCredentialsId(principal))));
  }

  @PatchMapping("/me")
  public ResponseEntity<ProfileResponseDto> updateMyProfile(
      Principal principal, @Valid @RequestBody UpdateProfileRequestDto dto) {
    Profile profile =
        this.updateProfileUseCase.execute(
            this.extractCredentialsId(principal),
            new UpdateProfileCommand(
                dto.firstName(),
                dto.lastName(),
                dto.companyName(),
                dto.siretNumber(),
                dto.vatNumber(),
                dto.billingEmail(),
                dto.billingAddressLine1(),
                dto.billingAddressLine2(),
                dto.billingPostalCode(),
                dto.billingCity(),
                dto.billingCountry()));
    return ResponseEntity.ok(this.toResponse(profile));
  }

  private UUID extractCredentialsId(Principal principal) {
    return UUID.fromString(principal.getName());
  }

  private ProfileResponseDto toResponse(Profile profile) {
    return new ProfileResponseDto(
        profile.getId(),
        profile.getCredentialsId(),
        profile.getFirstName(),
        profile.getLastName(),
        profile.getCompanyName(),
        profile.getSiretNumber(),
        profile.getVatNumber(),
        profile.getBillingEmail(),
        profile.getBillingAddressLine1(),
        profile.getBillingAddressLine2(),
        profile.getBillingPostalCode(),
        profile.getBillingCity(),
        profile.getBillingCountry(),
        profile.getCreatedAt(),
        profile.getUpdatedAt());
  }
}
