# LinguaCore API

A vocabulary-learning backend built around the **SM-2 spaced repetition algorithm**. Users create their own decks, add cards, and the system schedules each card for review at the moment they are most likely to forget it — spacing reviews further apart as recall improves.

Built with Spring Boot 3.5, PostgreSQL 16 and JWT authentication. The whole stack runs with a single command.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.5 — Web, Data JPA, Validation, Security |
| Database | PostgreSQL 16 |
| Authentication | JWT (jjwt 0.12), BCrypt password hashing |
| Documentation | OpenAPI 3 / Swagger UI (springdoc) |
| Testing | JUnit 5, MockMvc |
| Packaging | Docker, Docker Compose (multi-stage build) |

---

## Features

**Deck & card management** — Full CRUD for decks and cards, with cards nested under their parent deck (`/api/decks/{deckId}/cards`).

**SM-2 spaced repetition** — Implemented from scratch, not from a library. Each review updates the card's easiness factor, repetition count and interval; the next due date is derived from those. Fully covered by unit tests across every branch.

**Review workflow** — Fetch cards due today, submit a self-graded review (0–5), and the algorithm reschedules the card. Every review is recorded in a log table, which is the raw data for future statistics (streaks, study time, difficulty analysis).

**JWT authentication** — Register, log in, receive a signed token. A custom filter validates the token on every request and places the caller's identity into the security context. Stateless: no sessions stored server-side.

**Per-user data isolation** — Every deck belongs to a user. A user can only read, update or delete their own data; another user's resources return `404 Not Found` rather than `403`, so resource existence is never leaked through status codes.

**Consistent error handling** — A global exception handler maps each error type to the correct HTTP status: `404` not found, `409` duplicate, `401` unauthorized, `400` validation failure with per-field messages.

---

## Getting Started

### Prerequisites

- Docker Desktop (includes Docker Compose)

That is all. Java and Maven are not required — the build happens inside the container.

### Run

```bash
git clone https://github.com/21t1080014/linguacore-api.git
cd linguacore-api

# create your local secrets file
cp .env.example .env
# then open .env and fill in DB_PASSWORD and JWT_SECRET (at least 32 characters)

docker compose up -d --build
```

Open **http://localhost:8080/swagger-ui.html**

### Try it from Swagger

1. `POST /api/auth/register` → create an account
2. `POST /api/auth/login` → copy the returned `token`
3. Click **Authorize** (top right), paste the token, confirm
4. Every endpoint is now callable directly from the browser

### Useful commands

```bash
docker compose logs -f app     # follow application logs
docker compose down            # stop, keep data
docker compose down -v         # stop and wipe the database volume
```

---

## Configuration

No secrets are committed. `application.yml` reads everything from environment variables, and Docker Compose loads them from `.env`:

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | JDBC connection string | `jdbc:postgresql://localhost:5433/linguadb` |
| `DB_USERNAME` | Database user | `lingua` |
| `DB_PASSWORD` | Database password | — (required) |
| `JWT_SECRET` | Signing key, minimum 32 characters | — (required) |
| `JWT_EXPIRATION_MS` | Token lifetime | `86400000` (24h) |

---

## API Overview

All endpoints except `/api/auth/**` require an `Authorization: Bearer <token>` header.

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Authenticate, receive a JWT |

### Decks
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/decks` | List the caller's decks |
| GET | `/api/decks/{id}` | Get one deck |
| POST | `/api/decks` | Create a deck |
| PUT | `/api/decks/{id}` | Update a deck |
| DELETE | `/api/decks/{id}` | Delete a deck |

### Cards
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/decks/{deckId}/cards` | List cards in a deck |
| GET | `/api/decks/{deckId}/cards/{cardId}` | Get one card |
| POST | `/api/decks/{deckId}/cards` | Add a card |
| PUT | `/api/decks/{deckId}/cards/{cardId}` | Update a card |
| DELETE | `/api/decks/{deckId}/cards/{cardId}` | Delete a card |

### Reviews
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reviews/due` | Cards due for review today |
| POST | `/api/reviews` | Submit a review (grade 0–5), triggers SM-2 rescheduling |

Full request and response schemas are available in Swagger UI.

---

## The SM-2 Algorithm

After each review the easiness factor is adjusted by the grade:

```
q  = 5 − grade
EF = EF + (0.1 − q × (0.08 + q × 0.02))     floor: 1.3
```

| Grade | Effect on EF |
|---|---|
| 5 — instant recall | +0.10 |
| 4 — recalled after a pause | unchanged |
| 3 — recalled with difficulty | −0.14 |
| 0 — forgotten | −0.80 |

The interval then follows:

- **grade < 3** — repetitions reset to 0, interval back to 1 day
- **grade ≥ 3** — 1 day on the first success, 6 days on the second, then `round(previous interval × EF)`

A card graded 5 three times in a row moves through intervals of **1 → 6 → 17 days**: the better the recall, the less often it is shown, freeing time for weaker material.

---

## Architecture

Requests flow through strictly one-directional layers:

```
Controller  →  Service  →  Repository  →  PostgreSQL
   (HTTP)      (business)    (data)
```

- **Controller** — thin: receives the request, delegates, returns a response. No business logic, no queries.
- **Service** — business rules, transactions, and translation between entities and DTOs.
- **Repository** — Spring Data JPA interfaces; queries are derived from method names.
- **DTOs** — requests and responses never expose entities directly, which keeps the password hash and other internal fields off the wire and decouples the API contract from the schema.

### Data model

| Table | Purpose |
|---|---|
| `users` | Accounts, BCrypt password hashes, role |
| `decks` | Card collections, owned by a user |
| `cards` | Front, back, note, part of speech |
| `review_states` | Current scheduling state per card (1-to-1) |
| `review_logs` | One row per review, with interval before and after (1-to-many) |

The split between `review_states` and `review_logs` is deliberate: *state* is a single row that is continually updated, *history* is append-only. Statistics such as streaks and total study time are derived from the logs rather than stored as columns, so they can never drift out of sync with reality.

---

## Testing

```bash
mvn test
```

**Unit tests** — `Sm2SchedulerTest` covers all six branches of the scheduling algorithm, including the easiness floor and the reset-on-failure path. The scheduler is deliberately a plain class with no Spring or database dependency, so these tests run in milliseconds.

**Integration tests** — `DeckIntegrationTest` boots the full application and drives it through MockMvc: registration, login, token-authenticated deck creation, and a case proving that one user cannot see another user's decks. Each test runs in a transaction that is rolled back afterwards, so the database is left untouched.

---

## Roadmap

This repository covers phase one. Planned next:

- Video-based dictation and shadowing practice with sentence-level transcripts
- A shared dictionary seeded from open sources, with per-part-of-speech definitions
- Public deck library with copy-to-learn semantics
- Text-to-speech pronunciation (US/UK)
- Reading comprehension powered by retrieval-augmented generation

---

## Notes

Database schema is currently managed by Hibernate's `ddl-auto: update` for development convenience; migrating to Flyway is planned before any production deployment.