package com.antoineromand.atlascrm.account.application.usecase.account;

public record UpdateAccountCommand(
    String firstName,
    String lastName,
    String companyName,
    String siretNumber,
    String vatNumber,
    String billingEmail,
    String billingAddressLine1,
    String billingAddressLine2,
    String billingPostalCode,
    String billingCity,
    String billingCountry) {}
