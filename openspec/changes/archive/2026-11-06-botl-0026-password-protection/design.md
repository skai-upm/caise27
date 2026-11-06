# Design

## Context

See proposal.md – Why. `UserService` (singleton, configured with a `UserRepository`) validates, stores and authenticates with plain `String.equals`; `UserRepository.update(user, change)` applies a change to a copy, persists, then applies it to the stored object. The JAR is built by `maven-jar-plugin` only.

## Goals / Non-Goals

**Goals:**
- Hashing behind an interface injected into the user controller; JAR still self-contained.

**Non-Goals:**
- Pepper/secret management; migrating old files.

## Decisions

- **`PasswordHasher`** (business.service interface): `hash(plain)`, `matches(plain, stored)`. **`BCryptPasswordHasher`** uses `BCrypt.hashpw(plain, BCrypt.gensalt(rounds))` and `BCrypt.checkpw`; an `IllegalArgumentException` from `checkpw` (stored value not a BCrypt hash) means "does not match". Cost from system property `park.bcrypt.rounds` (default 10); surefire sets 4 so tests stay fast.
- **Injection**: `UserService.configure(UserRepository, PasswordHasher)`; `ParkController.start` injects `new BCryptPasswordHasher()`; the one-argument `configure(repo)` keeps working with the default hasher.
- **Create / update**: validate the raw password, then hash; for `PASSWORD` updates the hash is computed once before building the change, so the persisted copy and the in-memory user get the same value.
- **Authentication**: `hasher.matches(entered, user.getPassword())`.
- **Packaging**: `maven-shade-plugin` 3.6.0 at `package` replaces `target/amusement-park.jar` with a JAR containing jBCrypt and the existing manifest; `createDependencyReducedPom=false`.
- **Whole-user validation** (`UserValidator.isValid(User)`) keeps describing raw user data before protection; stored users hold hashes.
