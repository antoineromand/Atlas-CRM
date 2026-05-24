# Authentication

## Overview

Atlas CRM uses an email-first authentication flow with JWT access and refresh tokens.

The auth model is split into two parts:

- `credentials` stores auth-only fields
- `account` stores user identity and billing data

`ADMIN` accounts do not need a profile. `USER` accounts get an account profile and must provide `firstName` and `lastName` at registration.

## Data Model

### Credentials

`credentials` contains:

- `email`
- `password`
- `role`
- `status`
- `emailVerified`

`status` is the account state, not the email validation flag.

### Account

`account` contains:

- `firstName`
- `lastName`
- `companyName`
- `siretNumber`
- `vatNumber`
- `billingEmail`
- `billingAddressLine1`
- `billingAddressLine2`
- `billingPostalCode`
- `billingCity`
- `billingCountry`

Only the identifiers and timestamps are required in storage. The billing fields are intentionally optional for now.

## Auth Flow

### Register

- `USER` registration creates a `credentials` record and a matching account profile record.
- `ADMIN` registration only needs `credentials`.
- Email verification is manual for now. You can set `emailVerified = true` directly in the database when testing.

### Sign In

- Checks credentials email and password
- Refuses accounts that are suspended
- Refuses accounts whose email is not verified
- Returns an access token and a refresh token

### Refresh Token

- Validates the refresh token
- Rotates the refresh token
- Stores the active JTI in Redis

### Sign Out

- Validates the refresh token
- Deletes the JTI from Redis
- Revokes the token immediately

### Account

- `GET /api/v1/account/me` returns the current user's account
- `PATCH /api/v1/account/me` updates the current user's account
- `ADMIN` accounts return `404` on these routes because they do not have an account profile
- The payload is partial on `PATCH`, so only provided fields are updated

## Routes

- `POST /api/v1/authentication/register`
- `POST /api/v1/authentication/sign-in`
- `POST /api/v1/authentication/refresh-token`
- `POST /api/v1/authentication/sign-out`
- `GET /api/v1/account/me`
- `PATCH /api/v1/account/me`

## Local Testing

The repository includes a Postman collection:

- `postman/atlas-crm-auth.postman_collection.json`

It covers the current authentication and account endpoints and stores the access/refresh tokens in collection variables after sign-in.
