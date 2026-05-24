package com.antoineromand.atlascrm.api.account.controllers;

import com.antoineromand.atlascrm.account.application.usecase.account.IGetAccountUseCase;
import com.antoineromand.atlascrm.account.application.usecase.account.IUpdateAccountUseCase;
import com.antoineromand.atlascrm.account.application.usecase.account.UpdateAccountCommand;
import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.api.account.dto.AccountResponseDto;
import com.antoineromand.atlascrm.api.account.dto.UpdateAccountRequestDto;
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
      Principal principal, @Valid @RequestBody UpdateAccountRequestDto dto) {
    Account account =
        this.updateAccountUseCase.execute(
            this.extractCredentialsId(principal),
            new UpdateAccountCommand(
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
}
