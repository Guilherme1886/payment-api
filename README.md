# payment-api

A payment processing API built with Spring Boot and Kotlin. Handles user registration, account management, deposits, and peer-to-peer transfers with JWT authentication, strong consistency guarantees, and idempotent transaction processing.

## Stack

- **Language:** Kotlin 1.9.25
- **Framework:** Spring Boot 3.3.5 (Web, Data JPA, Security, Validation)
- **Runtime:** JVM 17
- **Database:** PostgreSQL 16
- **Migrations:** Flyway
- **Authentication:** JWT (jjwt 0.12.6) + BCrypt
- **Build:** Gradle (Kotlin DSL)
- **Testing:** JUnit 5, Spring MockMvc, Testcontainers
- **Containerization:** Docker Compose (local development)

## Architecture

The project follows **Clean Architecture** with four distinct layers. Dependencies flow inward: infrastructure and API depend on application, which depends on domain. The domain has zero framework dependencies.

```
┌─────────────────────────────────────────────────────────────┐
│                      api (HTTP layer)                       │
│    Controllers · DTOs · ExceptionHandler                    │
│    • Parses HTTP requests, returns HTTP responses           │
│    • No business logic                                      │
└──────────────────────────┬──────────────────────────────────┘
                           │ calls
┌──────────────────────────▼──────────────────────────────────┐
│                 application (use cases)                     │
│    RegisterUseCase · LoginUseCase                           │
│    CreateAccountUseCase · DepositUseCase                    │
│    ProcessPaymentUseCase                                    │
│    • Orchestrates business flows                            │
│    • Owns transactional boundaries                          │
└──────────────────────────┬──────────────────────────────────┘
                           │ depends on
┌──────────────────────────▼──────────────────────────────────┐
│                    domain (core model)                      │
│    Account · Transaction · User                             │
│    Repository interfaces · Exceptions · Value rules         │
│    • Pure business rules                                    │
│    • No Spring, no JPA, no framework code                   │
└──────────────────────────▲──────────────────────────────────┘
                           │ implements
┌──────────────────────────┴──────────────────────────────────┐
│                infra (technical adapters)                   │
│    JPA entities · Spring Data repositories                  │
│    Security filters · JWT provider · Password encoder       │
│    • Concrete implementations of domain contracts           │
└─────────────────────────────────────────────────────────────┘
```

Package layout:

```
com.payment.api
├── api               # Controllers, DTOs, exception handler
├── application       # Use cases (business flow orchestration)
├── domain            # Models, repository interfaces, domain exceptions
└── infra             # JPA, security, external integrations
```

## Endpoints

All endpoints except `/auth/**` require a valid JWT in the `Authorization: Bearer <token>` header.

### `POST /auth/register`

Creates a user and their associated account, returns a JWT.

**Request**
```json
{
  "email": "alice@example.com",
  "password": "strong-pass-123",
  "agencia": "0001",
  "numeroConta": "12345-6",
  "titularNome": "Alice Doe",
  "cpf": "11122233344"
}
```

**Response `201 Created`**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "9cafbfc8-1a2a-47fb-b73a-68df98119dad",
  "email": "alice@example.com",
  "accountId": "4f00ce9b-2581-4117-9a77-50d75c93cc73"
}
```

### `POST /auth/login`

Authenticates an existing user and returns a JWT.

**Request**
```json
{
  "email": "alice@example.com",
  "password": "strong-pass-123"
}
```

**Response `200 OK`**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "9cafbfc8-1a2a-47fb-b73a-68df98119dad",
  "email": "alice@example.com",
  "accountId": "4f00ce9b-2581-4117-9a77-50d75c93cc73"
}
```

### `POST /accounts`

Creates a standalone account (not linked to a user). Useful for administrative flows.

**Request**
```json
{
  "agencia": "0001",
  "numeroConta": "12345-6",
  "titularNome": "Alice Doe",
  "cpf": "11122233344"
}
```

**Response `201 Created`**
```json
{
  "id": "4f00ce9b-2581-4117-9a77-50d75c93cc73",
  "agencia": "0001",
  "numeroConta": "12345-6",
  "titularNome": "Alice Doe",
  "cpf": "11122233344",
  "balance": 0,
  "status": "ACTIVE",
  "currency": "BRL",
  "createdAt": "2026-04-16T12:00:00Z"
}
```

### `POST /accounts/{id}/deposit`

Credits the given amount to an account. Any authenticated user may deposit into any account.

**Request**
```json
{
  "amount": 500.00
}
```

**Response `200 OK`**
```json
{
  "id": "4f00ce9b-2581-4117-9a77-50d75c93cc73",
  "agencia": "0001",
  "numeroConta": "12345-6",
  "titularNome": "Alice Doe",
  "cpf": "11122233344",
  "balance": 500.00,
  "status": "ACTIVE",
  "currency": "BRL",
  "createdAt": "2026-04-16T12:00:00Z"
}
```

### `GET /accounts/{id}`

Fetches account details.

**Response `200 OK`**
```json
{
  "id": "4f00ce9b-2581-4117-9a77-50d75c93cc73",
  "agencia": "0001",
  "numeroConta": "12345-6",
  "titularNome": "Alice Doe",
  "cpf": "11122233344",
  "balance": 500.00,
  "status": "ACTIVE",
  "currency": "BRL",
  "createdAt": "2026-04-16T12:00:00Z"
}
```

### `POST /transactions`

Transfers funds from the authenticated user's account to another account. Idempotent by `idempotencyKey`.

**Request**
```json
{
  "idempotencyKey": "order-42-payment",
  "payerId": "4f00ce9b-2581-4117-9a77-50d75c93cc73",
  "receiverId": "babb11c7-98b6-4e1f-a41d-7c6e1ceaff40",
  "amount": 300.00
}
```

**Response `201 Created`**
```json
{
  "id": "e5c1a4bf-004b-4ab5-8b08-dadc5e30e9ad",
  "idempotencyKey": "order-42-payment",
  "payerId": "4f00ce9b-2581-4117-9a77-50d75c93cc73",
  "receiverId": "babb11c7-98b6-4e1f-a41d-7c6e1ceaff40",
  "amount": 300.00,
  "status": "COMPLETED",
  "createdAt": "2026-04-16T12:05:00Z",
  "updatedAt": "2026-04-16T12:05:00Z"
}
```

**Error responses**
- `403 Forbidden` — `payerId` does not match the authenticated user's account
- `422 Unprocessable Entity` — insufficient funds, or `payerId == receiverId`

### `GET /transactions/{id}`

Fetches transaction details.

**Response `200 OK`**
```json
{
  "id": "e5c1a4bf-004b-4ab5-8b08-dadc5e30e9ad",
  "idempotencyKey": "order-42-payment",
  "payerId": "4f00ce9b-2581-4117-9a77-50d75c93cc73",
  "receiverId": "babb11c7-98b6-4e1f-a41d-7c6e1ceaff40",
  "amount": 300.00,
  "status": "COMPLETED",
  "createdAt": "2026-04-16T12:05:00Z",
  "updatedAt": "2026-04-16T12:05:00Z"
}
```

## Transaction state machine

```
                     ┌───────────┐
                     │ SCHEDULED │
                     └─────┬─────┘
                           │
                  ┌────────┴────────┐
                  ▼                 ▼
            ┌─────────┐       ┌───────────┐
            │ PENDING │       │ CANCELLED │ (terminal)
            └────┬────┘       └───────────┘
                 │
        ┌────────┴────────┐
        ▼                 ▼
  ┌────────────┐    ┌───────────┐
  │ PROCESSING │    │ CANCELLED │ (terminal)
  └─────┬──────┘    └───────────┘
        │
   ┌────┴────┐
   ▼         ▼
┌──────────┐ ┌────────┐
│COMPLETED │ │ FAILED │  (both terminal)
└──────────┘ └────────┘
```

Transitions are enforced in `Transaction.transitionTo()`. Any transition from a terminal state (`COMPLETED`, `FAILED`, `CANCELLED`) throws `IllegalStateException`.

The `ProcessPaymentUseCase` moves a transaction `PENDING → PROCESSING → COMPLETED` in a single database transaction. On failure (e.g. domain validation), the entire DB transaction is rolled back and nothing is persisted — see the "insufficient funds" rule below.

## Business rules

### Only the payer can initiate a transfer
`payerId` in the request must equal the `accountId` of the JWT-authenticated user. Otherwise the request is rejected with `403 Forbidden`. This check lives inside `ProcessPaymentUseCase`, not the controller — the controller only forwards the authenticated account id.

### Self-transfer is blocked
If `payerId == receiverId`, the request fails with `422 Unprocessable Entity` and the message `"Cannot transfer to the same account"`. No transaction is persisted.

### Insufficient funds → 422, nothing persisted
Balance is verified **before** creating the transaction record. When the payer's balance is lower than the requested amount, `InsufficientFundsException` is thrown, which maps to `422 Unprocessable Entity`. Because the check runs inside a `@Transactional` boundary before any `save()` call, no row is written to the `transactions` table.

### Idempotency by unique key
Each transfer request carries an `idempotencyKey`. The column is `UNIQUE` in the `transactions` table. Before processing, the use case runs `findByIdempotencyKey(key)` — if a transaction already exists, it is returned as-is and no new work is performed. Retries of the same logical operation are therefore safe and cheap.

### Pessimistic locking on balances
Both the payer and receiver rows are acquired with `SELECT … FOR UPDATE` (`LockModeType.PESSIMISTIC_WRITE`), ordered by UUID to prevent deadlocks when two concurrent transfers touch the same pair of accounts in opposite directions. This guarantees that concurrent debits cannot observe a stale balance and therefore cannot overdraw the account.

## Running locally

Requirements: Docker, JDK 17.

```bash
docker compose up -d         # starts PostgreSQL on :5432
./gradlew bootRun            # starts the API on :8080
```

Flyway runs migrations automatically on startup (`V1__create_tables.sql`, `V2__create_users_table.sql`). Application config lives in `src/main/resources/application.yml`.

Quick smoke test:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"strong-pass-123","agencia":"0001","numeroConta":"12345-6","titularNome":"Alice Doe","cpf":"11122233344"}'
```

## Running tests

```bash
./gradlew test               # runs all tests
./gradlew test --rerun-tasks # forces re-execution (bypasses the task cache)
```

The integration suite (`PaymentIntegrationTest`) spins up a real PostgreSQL 16 container via Testcontainers, applies all Flyway migrations, and exercises the HTTP layer with `MockMvc`. The database is truncated before each test (`@BeforeEach`). Scenarios covered:

1. **Full transfer** — registers two users, deposits, transfers, asserts balances and transaction status in the database.
2. **Insufficient funds** — asserts `422` is returned and no transaction row is created.
3. **Idempotency** — the same `idempotencyKey` submitted twice results in a single persisted transaction and a single debit.
4. **Authentication** — requests without a token are rejected with `403`; valid tokens pass.

Test reports are generated at `build/reports/tests/test/index.html`.

## Technical decisions

### Why `@Transactional`
Transfers touch multiple aggregates (payer account, receiver account, transaction record) that must be consistent. Wrapping `ProcessPaymentUseCase.execute()` in `@Transactional` ensures either all writes commit together or none do — an exception anywhere in the flow rolls the database back to its prior state. This is how "insufficient funds → nothing persisted" works without manual bookkeeping.

### Why pessimistic locking
We chose `SELECT … FOR UPDATE` over optimistic locking (via `@Version`) because payment contention is expected, not exceptional. Optimistic locking retries on conflict; under load this degrades into a livelock and produces noisy `OptimisticLockException`s. Pessimistic locking blocks contenders briefly but guarantees forward progress and prevents overdraw in a single shot. Locks are acquired in a deterministic order (by UUID) to eliminate deadlocks.

### Why `BigDecimal`
Money is never a floating-point number. `Double`/`Float` cannot represent `0.10` exactly and introduce silent rounding errors that compound across transactions. `BigDecimal` with scale 2 matches the database column (`NUMERIC(19, 2)`) and gives deterministic arithmetic. Jackson serializes it as a plain number in JSON.

### Why Testcontainers
H2 and other in-memory databases diverge from PostgreSQL in subtle ways — JSON types, locking semantics, constraint behavior, `FOR UPDATE` support. We rely on PostgreSQL-specific features (pessimistic locks, `NUMERIC(19,2)`, UUID primary keys), so running tests against anything else defeats the purpose. Testcontainers boots a real PostgreSQL 16 image and runs our actual Flyway migrations, giving us confidence that tests exercise the same behavior production will.

### Why authorization lives in the use case, not the controller
Authorization (`payerId == authenticatedAccountId`) is a business rule: "a user can only move money from their own account." Putting it in the controller couples it to HTTP; putting it in the use case keeps it alongside the other invariants (`payerId != receiverId`, `balance >= amount`) it logically belongs with. The controller's job is reduced to transport concerns — unwrap the principal, forward it as a parameter. Any future caller of the use case (a batch job, a message handler) gets the same authorization check automatically.

## Next steps

### Webhooks for merchant notifications
Emit an event on every terminal transaction state (`COMPLETED`, `FAILED`) to a merchant-configured URL. Design for reliability: persist outgoing events in an `outbox` table within the same DB transaction as the payment, and have a separate worker deliver them with exponential backoff. This avoids the classic "we processed the payment but failed to notify" split-brain.

### Rate limiting
Protect `/auth/login` and `/transactions` specifically. Token-bucket per IP and per userId, backed by Redis so instances share state. Return `429 Too Many Requests` with a `Retry-After` header. Login should fail closed aggressively to deter credential stuffing; transaction limits should be looser but ensure a single compromised token cannot drain a balance in milliseconds.

### Kafka for asynchronous processing
Today transfers are synchronous, which caps throughput at the cost of holding a DB lock. Publishing transfer requests to a Kafka topic with the payer's account id as the partition key would serialize operations per account (preserving consistency) while fanning out across accounts (scaling linearly). The HTTP endpoint would return `202 Accepted` with the pending transaction id, and a consumer would drive it through the state machine.

### Reporting and statements
Expose `/accounts/{id}/statements?from=&to=` returning a paginated transaction history, and a daily aggregation job (per account: total credits, debits, ending balance) written to a read-optimized table. Keep the operational `transactions` table hot and lean; push analytics queries to a derived store to avoid contaminating payment latency with reporting workloads.
