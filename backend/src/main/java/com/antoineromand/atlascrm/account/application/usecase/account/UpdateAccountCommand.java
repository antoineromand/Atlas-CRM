package com.antoineromand.atlascrm.account.application.usecase.account;

public record UpdateAccountCommand(
    PatchValue<String> firstName,
    PatchValue<String> lastName,
    PatchValue<String> companyName,
    PatchValue<String> siretNumber,
    PatchValue<String> vatNumber,
    PatchValue<String> billingEmail,
    PatchValue<String> billingAddressLine1,
    PatchValue<String> billingAddressLine2,
    PatchValue<String> billingPostalCode,
    PatchValue<String> billingCity,
    PatchValue<String> billingCountry) {}
