# TripPilot AI — Backend

Spring Boot 4.1 (Java 21) + PostgreSQL API for the TripPilot Android app.

## Prerequisites
- JDK 21 or newer (`java -version`)
- PostgreSQL running on `localhost:5432` with database/user/password `trippilot`. Either:
  - **Postgres.app** — then in `psql`:
    ```sql
    CREATE USER trippilot WITH PASSWORD 'trippilot';
    CREATE DATABASE trippilot OWNER trippilot;
    ```
  - **Docker** — `docker compose up -d` in this folder.

Different credentials? Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables.

## Run
```bash
./mvnw spring-boot:run
```
Flyway creates the tables on first start. Health check: http://localhost:8080/actuator/health

## Test
```bash
./mvnw test
```
Tests start their own temporary PostgreSQL, so they need neither Docker nor a running database.

## API

| Method | Path | Body | Purpose |
|---|---|---|---|
| GET | `/api/trips` | — | All saved trips, most recently updated first |
| GET | `/api/trips/{id}` | — | One trip (404 if missing) |
| POST | `/api/trips` | `{"plan": TripPlan}` | Save a trip; saving the same `plan.id` again is a no-op |
| POST | `/api/trips/{id}/payment` | `{"status", "method", "orderId"?, "plan"?}` | Record a **simulated** payment; include `plan` if the trip isn't saved yet |
| DELETE | `/api/trips` | — | Delete all trips |

`TripPlan` and the trip response use the same JSON field names as the Android `domain/model` classes.
`status` is `NOT_PAID | PAID | FAILED | CANCELLED`; `method` is `UPI | CARD | NET_BANKING | WALLET`.

Try it:
```bash
curl localhost:8080/api/trips
```

## Layout
```
src/main/java/com/trippilot/backend/
  trip/      Trip entity, repository, service, controller, DTOs, enums
  common/    Shared exceptions (404 / 400)
src/main/resources/
  application.properties
  db/migration/V1__create_trips.sql   Flyway schema
```
