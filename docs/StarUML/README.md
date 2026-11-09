# KitchenPulse — StarUML Design Pack

This folder contains an editable StarUML project: `KitchenPulse.mdj`.

## Included diagrams

1. **Backend class diagram** — current Spring Boot implementation: `Branch`, `User`, `Unit`, `Ingredient`, their service/API/repository responsibilities, and key associations.
2. **ERD (V1)** — the four PostgreSQL tables created by the current Flyway migration: `branch`, `users`, `unit`, and `ingredient`.
3. **DFD (current release)** — authentication, user administration, branch administration, and shared catalog flows.
4. **Sequence: login and JWT authentication** — current implemented `POST /api/auth/login` path.
5. **Sequence: stock count to delivery** — conceptual future workflow based on the product requirements.

## Important scope note

The current repository README explicitly says this release contains identity, branches, and shared catalog. Stock counts, ingredient requests, central-office approval, warehouse fulfilment, dispatch, and branch receiving are later features. Therefore:

- The class diagram and ERD describe the **current code and V1 database**, not a fictional completed inventory backend.
- The login sequence and current-release DFD describe implemented behavior.
- The stock-count-to-delivery sequence is **planned/conceptual**, not evidence that those endpoints or tables already exist.

## Current database relationships

- `branch (1) → users (0..*)`: only branch staff are assigned a branch; central-office, warehouse, delivery, and admin users have a null `branch_id`.
- `unit (1) → ingredient (0..*)`: every ingredient has one fixed `stock_unit_id`.

The migration also enforces unique branch codes, case-insensitive unique user emails and ingredient names, valid roles, and the branch-role assignment rule. Those constraints are documented in the source SQL.

## How to open

1. Open StarUML.
2. Choose **File → Open…**.
3. Select `KitchenPulse.mdj` from this folder.
4. Use the Model Explorer to switch between the class, ERD, DFD, and sequence diagrams.

## Source of truth

Backend repository: https://github.com/Manishr767/KitchenPluseBackend

Before presenting these diagrams as the final system design, update the class diagram and ERD when the planned inventory modules are actually implemented through new Java entities and Flyway migrations.
