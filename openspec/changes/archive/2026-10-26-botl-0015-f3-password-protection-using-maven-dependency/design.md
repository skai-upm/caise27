# Design

## Context

`User` keeps the password in plaintext; `UserController.authenticate` compares with `equals`; `UserRepository` writes the field as-is. `App.configure` injects repositories into singleton controllers. The JAR is built by `maven-jar-plugin` without dependencies.

## Goals / Non-Goals

**Goals:**
- Hashing isolated behind a small interface injected into `UserController` (keeps the library out of the rest of the code and lets tests use a cheaper cost).
- Runnable fat JAR with the same name.

**Non-Goals:**
- Hashing other fields (phone, card), password reset flows.

## Decisions

- **Library**: `org.mindrot:jbcrypt:0.4` (single class, no transitive dependencies, available in the local Maven repository).
- **`business.security.PasswordHasher`** interface: `hash(String plain)`, `matches(String plain, String hash)`, `isHash(String value)`. **`BCryptPasswordHasher(int cost)`** implements it with `BCrypt.hashpw(plain, BCrypt.gensalt(cost))` / `BCrypt.checkpw`; `isHash` recognises `$2a$`/`$2b$`/`$2y$` 60-character values. Default cost 10 in the application; tests use cost 4 (the minimum) to stay fast.
- **`User`**: the field becomes `passwordHash` (`getPasswordHash`/`setPasswordHash`) so the model says what it stores.
- **`UserController.configure(UserRepository, PasswordHasher)`**: creation hashes after validation; `authenticate` uses `hasher.matches`; PASSWORD updates store `hasher.hash(newValue)`. On load, any stored value that is not a hash is replaced by its hash and the file saved once (legacy migration).
- **`App.configure(dataDir, clock)`** delegates to `configure(dataDir, clock, hasher)` with `new BCryptPasswordHasher(10)`; `TestPark` passes `new BCryptPasswordHasher(4)`.
- **Packaging**: `maven-shade-plugin` 3.6.0 bound to `package`, `shadedArtifactAttached=false` so `target/parque.jar` is replaced by the shaded JAR (manifest main class kept by the jar plugin; `createDependencyReducedPom=false` to avoid a generated POM in the tree).
- **UML**: `User.passwordHash`; new `PasswordHasher` interface realised by `BCryptPasswordHasher` (`cost : int`); `UserController x--> PasswordHasher : hasher`.

## Risks / Trade-offs

- [BCrypt is intentionally slow (≈100 ms per hash at cost 10)] → Acceptable for interactive use; tests use cost 4.
- [jbcrypt 0.4 is old but stable] → Widely used; the interface allows swapping the implementation.
