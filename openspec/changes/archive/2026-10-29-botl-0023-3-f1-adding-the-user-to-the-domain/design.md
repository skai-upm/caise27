# Design

## Context

`User` has `email`, `dni` (final), `passwordHash`; equality by email. `TextFileUserRepository` writes `email;dni;hash`. `App` only reads the optional command file (input, not persistence) and wires repositories.

## Goals / Non-Goals

**Goals:** full user model, DNI immutability, persistence of all fields. **Non-Goals:** validation, USER commands.

## Decisions

- **Fields**: private `firstName`, `surnames`, final `dni`, `email`, `passwordHash`, `phone`, `card`. Full constructor `User(firstName, surnames, dni, email, passwordHash, phone, card)`; partial constructor `User(email, dni)` (empty strings for unknown text attributes). Getters for all; setters for all except DNI (`setPasswordHash` keeps accepting protected values only).
- **Identity**: `equals`/`hashCode` on DNI (immutable).
- **Persistence**: users line `email;dni;hash;firstName;surnames;phone;card`; 2- and 3-field lines still accepted.
- **Separation check**: architecture test asserts `User` imports no `java.io`/`java.nio`; `App` contains no `Files.write`/`Files.newBufferedWriter`.
- **Controller**: `registerUser` unchanged (keyed by email).

## Risks / Trade-offs

- [Equality change] → all collections already contain distinct users with distinct DNIs; existing tests confirm behavior.
