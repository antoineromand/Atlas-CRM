package com.antoineromand.atlascrm.api.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequestDto(
    @Size(max = 120, message = "First name must contain at most 120 characters")
        String firstName,
    @Size(max = 120, message = "Last name must contain at most 120 characters") String lastName,
    @Size(max = 200, message = "Company name must contain at most 200 characters")
        String companyName,
    @Size(max = 14, message = "SIRET number must contain at most 14 characters")
        String siretNumber,
    @Size(max = 32, message = "VAT number must contain at most 32 characters")
        String vatNumber,
    @Email(message = "Billing email must be valid") @Size(max = 200, message = "Billing email must contain at most 200 characters")
        String billingEmail,
    @Size(max = 255, message = "Billing address line 1 must contain at most 255 characters")
        String billingAddressLine1,
    @Size(max = 255, message = "Billing address line 2 must contain at most 255 characters")
        String billingAddressLine2,
    @Size(max = 20, message = "Billing postal code must contain at most 20 characters")
        String billingPostalCode,
    @Size(max = 120, message = "Billing city must contain at most 120 characters")
        String billingCity,
    @Size(max = 120, message = "Billing country must contain at most 120 characters")
        String billingCountry) {}
