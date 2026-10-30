# LinguaCore API

A vocabulary-learning backend built around the **SM-2 spaced repetition algorithm**. Users create their own decks, add cards, and the system schedules each card for review at the moment they are most likely to forget it — spacing reviews further apart as recall improves.

Built with Spring Boot 3.5, PostgreSQL 16 and JWT authentication, plus a Python worker that generates AI example sentences asynchronously through RabbitMQ, Redis and the Gemini API. The whole stack runs with a single command.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS), Python 3.14 (AI worker) |
| Framework | Spring Boot 3.5 — Web, Data JPA, Validation, Security, AMQP |
| Database | PostgreSQL 16 |
| Messaging | RabbitMQ 4 |
| Cache | Redis 8 (used by the AI worker) |
| AI | Google Gemini API (`google-genai`) |
| Authentication | JWT (jjwt 0.12), BCrypt password hashing |
| Documentation | OpenAPI 3 / Swagger UI (springdoc) |
| Testing | JUnit 5, MockMvc |
| Packaging | Docker, Docker Compose (multi-stage build) |

---

## Features

**Deck & card management** — CRUD for decks and cards, with cards nested under their parent deck (`/api/decks/{deckId}/cards`). Some delete and single-card operations currently fail; see [Known issues](#known-issues).

**SM-2 spaced repetition** — Implemented from scratch, not from a library. Each review updates the card's easiness factor, repetition count and interval; the next due date is derived from those. Fully covered by unit tests across every branch.

**Review workflow** — Fetch cards that are due, then submit a review in one of three modes: `FLASHCARD` (front → back) and `REVERSE` (back → front) are self-graded (0–5); in `TYPING` the learner sees the back and types the front. The server compares the typed answer with the card's front using Unicode NFKC normalization, curly-apostrophe normalization, trimming, case-insensitive matching and whitespace collapsing; diacritics still count. A correct answer maps to grade 4 and a wrong one to grade 1, unless the learner overrides it by sending their own grade. The algorithm then reschedules the card. Every review is recorded in a log table with both the final `grade` and the server's `correct` verdict, which is the raw data for future statistics (streaks, study time, difficulty analysis).

**AI example sentences** — Requesting an example queues a background task and returns immediately with `202 Accepted`; a Python worker generates the sentence with Gemini, and the client polls for the result. Sentences are cached per word in Redis for 30 days. Failed tasks are retried with exponential backoff, then marked `FAILED` and moved to a dead-letter queue.

**JWT authentication** — Register, log in, receive a signed token. A custom filter validates the token on every request and places the caller's identity into the security context. Stateless: no sessions stored server-side.

**Per-user data isolation** — Every deck belongs to a user. A user can only read, update or delete their own data; another user's decks, reviewed cards and AI tasks return `404 Not Found` rather than `403`, so resource existence is not leaked through status codes. Single-card endpoints are currently an exception; see [Known issues](#known-issues).

**Error handling** — A global exception handler maps application errors to HTTP statuses: `404` not found, `409` duplicate, `401` failed login or unknown user, `400` validation failure with per-field messages. Errors outside this handler do not follow the same format or status mapping yet; see [Known issues](#known-issues).

---

## Getting Started

### Prerequisites

- Docker Desktop (includes Docker Compose)

That is all. Java and Maven are not required to run the stack — the build happens inside the container.

### Run

```bash
git clone https://github.com/21t1080014/linguacore-api.git
cd linguacore-api

# create your local secrets file
cp .env.example .env
# then open .env and fill in DB_PASSWORD, JWT_SECRET (at least 32 characters),
# REDIS_PASSWORD, RABBITMQ_USER, RABBITMQ_PASSWORD and GEMINI_API_KEY

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

No secrets are committed. `application.yml` and `worker.py` read their settings from environment variables, and Docker Compose takes the values from `.env` and passes each service only what it needs:

| Variable | Purpose | Received by | Default |
|---|---|---|---|
| `DB_URL` | JDBC connection string | `app` | `jdbc:postgresql://localhost:5433/linguadb`; Compose always sets `jdbc:postgresql://db:5432/linguadb` |
| `DB_HOST` | Database host for the worker | `worker` | `localhost`; Compose always sets `db` |
| `DB_PORT` | Database port for the worker | `worker` | `5433`; Compose always sets `5432` |
| `DB_USERNAME` | Database user | `app`, `worker`, `db` | `lingua` |
| `DB_PASSWORD` | Database password | `app`, `worker`, `db` | — (required) |
| `JWT_SECRET` | Signing key, minimum 32 characters | `app` | — (required) |
| `JWT_EXPIRATION_MS` | Token lifetime | `app` | `86400000` (24h) |
| `RABBITMQ_HOST` | RabbitMQ host | `app`, `worker` | `localhost`; Compose always sets `rabbitmq` |
| `RABBITMQ_USER` | RabbitMQ user | `app`, `worker`, `rabbitmq` | — (required) |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `app`, `worker`, `rabbitmq` | — (required) |
| `REDIS_HOST` | Redis host | `worker` | `localhost`; Compose always sets `redis` |
| `REDIS_PASSWORD` | Redis password | `worker`, `redis` | — (required) |
| `GEMINI_API_KEY` | Gemini API key | `worker` | — (required) |

---

## API Overview

All endpoints except `/api/auth/**` and the Swagger UI / OpenAPI docs require an `Authorization: Bearer <token>` header.

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
| GET | `/api/reviews/due` | Cards due for review today or overdue |
| POST | `/api/reviews` | Submit a review, triggers SM-2 rescheduling |

`POST /api/reviews` request body:

| Field | Rules |
|---|---|
| `cardId` | Required |
| `mode` | Required: `FLASHCARD`, `REVERSE` or `TYPING` (uppercase) |
| `grade` | 0–5. Required for `FLASHCARD` and `REVERSE`; optional override for `TYPING` |
| `typedAnswer` | Required and non-blank for `TYPING`, at most 500 characters |
| `durationMs` | Optional, 0–3600000 |

The response contains `dueDate`, `intervalDays`, `repetitions`, `easiness`, `correct` and `correctAnswer`. The last two are `null` in the self-graded modes.

### AI tasks
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/cards/{id}/examples` | Queue example generation for a card, returns `202 Accepted` with the task |
| GET | `/api/tasks/{id}` | Poll a task: `status` is `PENDING`, `DONE` or `FAILED`; `result` holds the sentence |

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

The interval then follows, using the updated EF:

- **grade < 3** — repetitions reset to 0, interval back to 1 day
- **grade ≥ 3** — 1 day on the first success, 6 days on the second, then `round(previous interval × EF)`

A new card (EF 2.5) graded 5 three times in a row moves through intervals of **1 → 6 → 17 days**: the better the recall, the less often it is shown, freeing time for weaker material.

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

AI example generation runs outside the request cycle:

```
POST /api/cards/{id}/examples
   Spring API  →  save ai_task (PENDING)  →  PostgreSQL
       │  publish task id
       ▼
   RabbitMQ (ai.task.queue)  →  Python worker  →  Redis cache, else Gemini
                                     │
                                     ▼
              write result, status = DONE  →  PostgreSQL

GET /api/tasks/{id}  →  client polls until DONE or FAILED
```

The worker retries a failed task after 2, 4 and 8 seconds; the next failure sets `FAILED` and moves the task to `ai.task.dlq`. If Redis is unavailable, the worker skips the cache for 60 seconds and keeps working.

### Data model

| Table | Purpose |
|---|---|
| `users` | Accounts, BCrypt password hashes, role |
| `decks` | Card collections, owned by a user |
| `cards` | Front, back, note, part of speech |
| `review_states` | Current scheduling state per card (1-to-1) |
| `review_logs` | One row per review: `grade`, `mode`, `correct` (`null` when self-graded), `duration_ms`, interval before and after (1-to-many) |
| `ai_task` | Background AI tasks: type, status, card, owner, result, retry count |

The split between `review_states` and `review_logs` is deliberate: *state* is a single row that is continually updated, *history* is append-only. Statistics such as streaks and total study time are meant to be derived from the logs rather than stored as columns, so they cannot drift out of sync with reality; these statistics are not implemented yet.

---

## Testing

```bash
./mvnw test
```

Running the tests locally requires JDK 21.

**Unit tests** — `Sm2SchedulerTest` has six cases covering every branch of the scheduling algorithm, including the easiness floor and the reset-on-failure path. The scheduler is deliberately a plain class with no Spring or database dependency, so these tests run in milliseconds. `AnswerCheckerTest` covers typed-answer normalization: case, spacing, full-width characters, combining diacritics and curly apostrophes, plus rejection of misspellings and missing diacritics.

**Integration tests** — `DeckIntegrationTest` boots the full application and drives it through MockMvc: registration, login, token-authenticated deck creation, and rejection of a request without a token. Each test runs in a transaction that is rolled back afterwards, so the database is left untouched.

The integration tests and `LinguacoreApplicationTests` need PostgreSQL on `localhost:5433` with the schema already created (start the stack once; the test configuration does not set `ddl-auto`). Maven does not read `.env`, so set `DB_PASSWORD` and `JWT_EXPIRATION_MS` in your shell: `src/test/resources/application.yml` replaces the main configuration and does not define the token lifetime.

---

## Known issues

- **Single-card endpoints fail** — `GET`, `PUT` and `DELETE /api/decks/{deckId}/cards/{cardId}` fail for every caller when the card exists, including its owner. `CardService` calls its own `findOwnedCard` directly, so the `@Transactional` annotation does not apply and reading the lazy deck throws `LazyInitializationException`. The response is `403`, while a card that does not exist returns `404`, so existence can be told apart on these endpoints.
- **Authentication failures return `403`, not `401`** — a missing or invalid token gets `403`, because no authentication entry point is configured. `401` is returned only for a failed login or a token whose user no longer exists.
- **Malformed JSON returns `403`, not `400`** — for example a lowercase `mode`. The exception handler does not cover unreadable request bodies or unexpected exceptions, and the error path `/error` itself requires authentication.
- **Error bodies are not uniform** — validation failures use an `errors` object, while other handled errors use `message`.
- **Decks with cards cannot be deleted** — no cascade is configured from decks to cards, so the delete violates the foreign key.
- **One integration test is wrong** — `userA_khongThayDeckCuaUserB` registers the same email twice and expects `201` both times, then logs in with an email it never registered, so it does not prove isolation between users.

---

## Roadmap

Implemented so far: deck and card management, SM-2 scheduling, flashcard, reverse and typing review modes, and AI example sentences. Planned next:

- Pronunciation audio (US/UK)
- Listening mode
- Cloze mode
- AI essay grading
- CI with GitHub Actions
- Public deck library with copy-to-learn semantics
- Video-based dictation and shadowing practice with sentence-level transcripts
- A shared dictionary seeded from open sources, with per-part-of-speech definitions
- Reading comprehension powered by retrieval-augmented generation

---

## Notes

Database schema is currently managed by Hibernate's `ddl-auto: update` for development convenience; migrating to Flyway is planned before any production deployment.