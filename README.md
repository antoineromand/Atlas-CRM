# Atlas CRM

Atlas CRM is a freelance business management platform. The goal is to give independent professionals a single place to manage clients, missions, quotes, invoices, and the rest of their day-to-day business operations.

The visual template for the project comes from Google Stitch.

## Backend

The API is built as a modular monolith:

- Java 25
- Spring Boot
- PostgreSQL
- JWT authentication with refresh tokens

The codebase follows a modular package structure rather than separate deployable modules. Each logical area of the domain should live in clear, well-named packages inspired by DDD and Clean Architecture principles. Shared concerns must stay in a dedicated `shared` area with a consistent and easy-to-navigate organization.

## Project Structure

- `backend/`: Spring Boot API
- `dockerfiles/backend/`: Docker image for the backend
- `docker-compose.yml`: local development stack

## Local Setup

1. Create your local environment file from the example:

   ```bash
   cp .env.example .env
   ```

2. Start the stack:

   ```bash
   docker compose up --build
   ```

The API will be available on `http://localhost:3000`.

## Environment Variables

The backend expects the following environment variables:

- `DB_HOST`
- `DB_PORT`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_NAME`
- `SERVER_PORT`

## Notes

- The backend is currently designed as a single deployable monolith.
- The package organization should stay explicit and simple.
- Authentication is expected to rely on access tokens and refresh tokens.
