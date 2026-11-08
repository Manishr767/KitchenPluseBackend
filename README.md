# KitchenPulse

Multi-branch restaurant ingredient supply management. Modular monolith on Spring Boot 3.5 / Java 21 / PostgreSQL.

**Feature 1 (this drop):** identity (users, login, JWT), branches, shared catalog (units, ingredients).
Later features (stock checks, requests, warehouse, procurement, deliveries) will arrive as new Flyway migrations and new modules.

## Quick start

Prerequisites: JDK 21, Maven 3.9+, Docker.

```bash
docker compose up -d                       # PostgreSQL 16 on :5432
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile bootstraps an admin: `admin@kitchenpulse.local` / `ChangeMe123!` (development only).

* Swagger UI: http://localhost:8080/swagger-ui.html (use **Authorize** and paste the token from `/api/auth/login`)
* Health: http://localhost:8080/actuator/health

```bash
TOKEN=$(curl -s localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"admin@kitchenpulse.local","password":"ChangeMe123!"}' | jq -r .accessToken)

curl -s localhost:8080/api/ingredients -H "Authorization: Bearer $TOKEN"
```

For anything other than local dev, set `JWT_SECRET` (>= 32 chars, e.g. `openssl rand -base64 48`),
`BOOTSTRAP_ADMIN_EMAIL` / `BOOTSTRAP_ADMIN_PASSWORD`, and the `DB_*` variables (see `.env.example`).

## Tests

```bash
mvn test
```

Integration tests start a real PostgreSQL through Testcontainers, so **Docker must be running**.
They run the actual Flyway migrations, Hibernate `validate`, and the real security chain.
`JwtServiceTest` and `ArchitectureTest` need no Docker.

If you don't have Maven installed: `mvn -N wrapper:wrapper` once, then use `./mvnw`.

## API overview

| Area | Endpoints | Who |
|---|---|---|
| Auth | `POST /api/auth/login` | public |
| Me | `GET /api/users/me`, `POST /api/users/me/password` | any logged-in user |
| Users | `GET/POST /api/users`, `GET/PUT /api/users/{id}`, `POST /api/users/{id}/activate\|deactivate`, `PUT /api/users/{id}/password` | ADMIN |
| Branches | `GET /api/branches` | ADMIN, CENTRAL_OFFICE_STAFF, WAREHOUSE_MANAGER |
| | `GET /api/branches/{id}` | the above, plus BRANCH_STAFF for their own branch only |
| | `POST`, `PUT /{id}`, `POST /{id}/activate\|deactivate` | ADMIN |
| Units | `GET /api/units` | any logged-in user |
| | `POST /api/units` | ADMIN |
| Ingredients | `GET /api/ingredients?q=&active=&page=&size=&sort=`, `GET /{id}` | any logged-in user |
| | `POST`, `PUT /{id}` (rename), `POST /{id}/activate\|deactivate` | ADMIN |

Errors are RFC 7807 problem JSON: 400 validation (with an `errors` map), 401 not logged in / bad credentials,
403 no permission, 404 not found, 409 duplicate, 422 business rule.

## Architecture rules

```
common        shared kernel: exceptions, error handling, paging, Role, AuthenticatedUser
config        Clock, OpenAPI
branch        -> common
catalog       -> common
identity      -> branch, common
```

* A module exposes only its **service** (and DTOs). Repositories are package-private, so other modules cannot use them.
* Modules reference each other by **id** (`Long`), never a JPA relation across modules. Database foreign keys still apply.
* `ArchitectureTest` fails the build on dependency cycles between modules.

## Decisions made while building (change any of them if you disagree)

1. **V1 is your migration, unchanged.** V2 only seeds common units (kg, g, l, ml, pc, box, pack), so ingredient unit conversions move to **V3**.
2. **`Role` and `AuthenticatedUser` live in `common`.** `branch` needs the logged-in user's branch id and `identity` needs `branch`; keeping them in `identity` would create a cycle.
3. **Token contains only the user id.** Role, branch and active flag are read from the database on every request, so deactivating a user or changing a role takes effect immediately (cost: one indexed lookup per request).
4. **An ingredient's stock unit cannot change** after creation (only the name can). Every stock quantity will be expressed in that unit.
5. **Lock-out protection:** an admin cannot deactivate their own account or change their own role.
6. **Login errors are generic** (same message for unknown email, wrong password, deactivated account) and timing-equalised.
7. **No password is stored in a migration.** The first admin comes from `BOOTSTRAP_ADMIN_*` environment variables.
8. **Timestamps are written by the application in UTC** (`updated_at` is maintained in code, not by a DB trigger).
9. Business-rule role check mirrors the DB constraint: BRANCH_STAFF must have a branch; every other role must not.

## Not in this drop

Refresh tokens / logout, CORS (add when the React frontend starts), rate limiting on login, audit log, Dockerfile.
