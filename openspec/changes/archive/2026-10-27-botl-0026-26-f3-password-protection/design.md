# Design

## Context

`User.password` holds the plain password; `UserService` compares with `equals`; `UserFileStorage` writes it as-is. Delivery 3 requires BCrypt via a Maven dependency (jBCrypt 0.4 suggested) and explicitly overrides the Delivery 1 "no external libraries / no encryption" restriction for it.

## Goals / Non-Goals

**Goals:**
- Store/persist only BCrypt values; verify with BCrypt; JAR still standalone.

**Non-Goals:**
- Migrating plain-text user files (explicitly not required).

## Decisions

- **Dependency:** `org.mindrot:jbcrypt:0.4` (compile scope).
- **Standalone JAR:** `maven-shade-plugin` (3.6.0) in the `package` phase replaces `target/amusement-park.jar` with a JAR that includes jBCrypt, keeping the `Main-Class` from the jar plugin. Alternative (Class-Path manifest + copied libs) rejected: needs files next to the JAR.
- **Injection:** `PasswordHasher` interface (`hash(raw)`, `matches(raw, stored)`) with `BCryptPasswordHasher` (`BCrypt.hashpw(raw, BCrypt.gensalt())`, `BCrypt.checkpw`); injected into `UserService.configure(...)` by `ParkController.initialize`. `matches` returns `false` for stored values that are not BCrypt hashes (old plain-text data), instead of throwing.
- **Flow:** `createUser` validates the raw password, then stores `hash(raw)`; `authenticate` uses `matches`; `updateUser(PASSWORD)` validates and stores a new hash. `User.password` now holds the protected value (no format change in the file: same position, BCrypt string contains no `;`).
- **Tests:** assertions that compared plain passwords now check `BCrypt.checkpw` and that the file does not contain the raw password.
- **Mandatory commit:** `[F3-C05] Secure stored user passwords` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [BCrypt cost slows tests] → Default cost (10) kept for production; acceptable test duration.
- [Old data files with plain passwords] → Those users cannot authenticate; documented, as migration is not required.
