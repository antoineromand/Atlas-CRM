# Atlas CRM

Atlas CRM is a freelance business management platform for independent professionals.

## Summary

- Modular monolith backend in Java and Spring Boot
- PostgreSQL for persistence
- Redis for refresh token storage and revocation
- JWT authentication with access and refresh tokens
- Separate `credentials` and `profile` data model

## Table Of Contents

- [Quick Start](#quick-start)
- [Project Layout](#project-layout)
- [Documentation](#documentation)
- [Environment](#environment)

## Quick Start

1. Create your local environment file:

   ```bash
   cp .env.example .env
   ```

2. Start the stack:

   ```bash
   docker compose up --build
   ```

The API runs at `http://localhost:3000`.

## Project Layout

- `backend/`: Spring Boot API
- `dockerfiles/backend/`: Docker image for the backend
- `docker-compose.yml`: local development stack
- `postman/atlas-crm-auth.postman_collection.json`: auth collection for Postman

## Documentation

- [Authentication](docs/authentication.md)
- [Git Versioning](docs/git-versioning.md)
- `backend/HELP.md`: Spring Boot helper notes

## Environment

The backend expects:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `JWT_SECRET`
- `EXPIRATION_ACCESS_TOKEN`
- `EXPIRATION_REFRESH_TOKEN`
- `SERVER_PORT`
