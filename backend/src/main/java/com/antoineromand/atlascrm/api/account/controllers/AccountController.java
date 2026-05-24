package com.antoineromand.atlascrm.api.account.controllers;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.application.usecase.account.IUpdateAccountUseCase;
import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import com.antoineromand.atlascrm.account.application.usecase.account.UpdateAccountCommand;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.account.dto.AccountResponseDto;
import java.security.Principal;
import java.util.UUID;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

  private final IGetAccountUseCase getAccountUseCase;
  private final IUpdateAccountUseCase updateAccountUseCase;

  public AccountController(
      IGetAccountUseCase getAccountUseCase, IUpdateAccountUseCase updateAccountUseCase) {
    this.getAccountUseCase = getAccountUseCase;
    this.updateAccountUseCase = updateAccountUseCase;
  }

  @GetMapping("/me")
  public ResponseEntity<AccountResponseDto> getMyAccount(Principal principal) {
    return ResponseEntity.ok(this.toResponse(this.getAccountUseCase.execute(this.extractCredentialsId(principal))));
  }

  @PatchMapping("/me")
  public ResponseEntity<AccountResponseDto> updateMyAccount(
      Principal principal, @RequestBody Map<String, Object> body) {
    Account account =
        this.updateAccountUseCase.execute(
            this.extractCredentialsId(principal),
            new UpdateAccountCommand(
                this.patchString(body, "firstName", 120),
                this.patchString(body, "lastName", 120),
                this.patchString(body, "companyName", 200),
                this.patchString(body, "siretNumber", 14),
                this.patchString(body, "vatNumber", 32),
                this.patchEmail(body, "billingEmail", 200),
                this.patchString(body, "billingAddressLine1", 255),
                this.patchString(body, "billingAddressLine2", 255),
                this.patchString(body, "billingPostalCode", 20),
                this.patchString(body, "billingCity", 120),
                this.patchString(body, "billingCountry", 120)));
    return ResponseEntity.ok(this.toResponse(account));
  }

  private UUID extractCredentialsId(Principal principal) {
    return UUID.fromString(principal.getName());
  }

  private AccountResponseDto toResponse(Account account) {
    return new AccountResponseDto(
        account.getId(),
        account.getCredentialsId(),
        account.getFirstName(),
        account.getLastName(),
        account.getCompanyName(),
        account.getSiretNumber(),
        account.getVatNumber(),
        account.getBillingEmail(),
        account.getBillingAddressLine1(),
        account.getBillingAddressLine2(),
        account.getBillingPostalCode(),
        account.getBillingCity(),
        account.getBillingCountry(),
        account.getCreatedAt(),
        account.getUpdatedAt());
  }

  private PatchValue<String> patchString(Map<String, Object> body, String fieldName, int maxLength) {
    if (!body.containsKey(fieldName)) {
      return PatchValue.absent();
    }

    Object rawValue = body.get(fieldName);
    if (rawValue == null) {
      return PatchValue.of(null);
    }

    if (!(rawValue instanceof String value)) {
      throw new IllegalArgumentException(fieldName + " must be a string or null");
    }

    if (value.length() > maxLength) {
      throw new IllegalArgumentException(fieldName + " exceeds max length of " + maxLength);
    }

    return PatchValue.of(value);
  }

  private PatchValue<String> patchEmail(Map<String, Object> body, String fieldName, int maxLength) {
    if (!body.containsKey(fieldName)) {
      return PatchValue.absent();
    }

    Object rawValue = body.get(fieldName);
    if (rawValue == null) {
      return PatchValue.of(null);
    }

    if (!(rawValue instanceof String value)) {
      throw new IllegalArgumentException(fieldName + " must be a string or null");
    }

    if (value.length() > maxLength) {
      throw new IllegalArgumentException(fieldName + " exceeds max length of " + maxLength);
    }

    if (!value.contains("@")) {
      throw new IllegalArgumentException(fieldName + " must be a valid email");
    }

    return PatchValue.of(value);
  }
}
