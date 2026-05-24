package com.antoineromand.atlascrm.account.infrastructure.model;

import com.antoineromand.atlascrm.account.domain.Account;
import com.antoineromand.atlascrm.authentication.infrastructure.model.CredentialsEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "accounts")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AccountEntity {
  @Id
  @Column(name = "account_id", columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id = UUID.randomUUID();

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "credentials_id", nullable = false, unique = true)
  private CredentialsEntity credentials;

  @Column(name = "first_name", length = 120)
  private String firstName;

  @Column(name = "last_name", length = 120)
  private String lastName;

  @Column(name = "company_name", length = 200)
  private String companyName;

  @Column(name = "siret_number", length = 14)
  private String siretNumber;

  @Column(name = "vat_number", length = 32)
  private String vatNumber;

  @Column(name = "billing_email", length = 200)
  private String billingEmail;

  @Column(name = "billing_address_line1", length = 255)
  private String billingAddressLine1;

  @Column(name = "billing_address_line2", length = 255)
  private String billingAddressLine2;

  @Column(name = "billing_postal_code", length = 20)
  private String billingPostalCode;

  @Column(name = "billing_city", length = 120)
  private String billingCity;

  @Column(name = "billing_country", length = 120)
  private String billingCountry;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public AccountEntity() {}

  public AccountEntity(
      UUID id,
      CredentialsEntity credentials,
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
    this.id = id != null ? id : UUID.randomUUID();
    this.credentials = credentials;
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

  public static AccountEntity fromDomain(Account account, CredentialsEntity credentials) {
    return new AccountEntity(
        account.getId(),
        credentials,
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

  public Account toDomain() {
    return new Account(
        id,
        credentials.getId(),
        firstName,
        lastName,
        companyName,
        siretNumber,
        vatNumber,
        billingEmail,
        billingAddressLine1,
        billingAddressLine2,
        billingPostalCode,
        billingCity,
        billingCountry,
        createdAt,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public CredentialsEntity getCredentials() {
    return credentials;
  }
}
