package com.antoineromand.atlascrm.account.domain;

import java.time.Instant;
import java.util.UUID;

public class Profile {
  private final UUID id;
  private final UUID credentialsId;
  private final String firstName;
  private final String lastName;
  private final String companyName;
  private final String siretNumber;
  private final String vatNumber;
  private final String billingEmail;
  private final String billingAddressLine1;
  private final String billingAddressLine2;
  private final String billingPostalCode;
  private final String billingCity;
  private final String billingCountry;
  private final Instant createdAt;
  private final Instant updatedAt;

  public Profile(
      UUID id,
      UUID credentialsId,
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
      String billingCountry,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.credentialsId = credentialsId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.companyName = companyName;
    this.siretNumber = siretNumber;
    this.vatNumber = vatNumber;
    this.billingEmail = billingEmail;
    this.billingAddressLine1 = billingAddressLine1;
    this.billingAddressLine2 = billingAddressLine2;
    this.billingPostalCode = billingPostalCode;
    this.billingCity = billingCity;
    this.billingCountry = billingCountry;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCredentialsId() {
    return credentialsId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getSiretNumber() {
    return siretNumber;
  }

  public String getVatNumber() {
    return vatNumber;
  }

  public String getBillingEmail() {
    return billingEmail;
  }

  public String getBillingAddressLine1() {
    return billingAddressLine1;
  }

  public String getBillingAddressLine2() {
    return billingAddressLine2;
  }

  public String getBillingPostalCode() {
    return billingPostalCode;
  }

  public String getBillingCity() {
    return billingCity;
  }

  public String getBillingCountry() {
    return billingCountry;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
