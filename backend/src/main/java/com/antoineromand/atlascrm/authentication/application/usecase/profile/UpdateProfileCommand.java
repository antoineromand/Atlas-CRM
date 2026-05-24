package com.antoineromand.atlascrm.authentication.application.usecase.profile;

public record UpdateProfileCommand(
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
