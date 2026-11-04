# Design

## Context

`UserManager` stores `User.password` as given and authenticates with `user.getPassword().equals(password)`. Since the previous changes, `App` creates the services and injects their collaborators. Many tests build `UserManager` instances directly and some assert plaintext passwords in files or getters.

## Goals / Non-Goals

**Goals:**
- Hashing hidden behind a business abstraction, so the library can be replaced.
- The same E1 validation messages and command outputs.

**Non-Goals:**
- Migrating existing plaintext entries, other authentication schemes, or password policies beyond E1.

## Decisions

- **Library.** `org.mindrot:jbcrypt:0.4`, the reference Java BCrypt implementation. It is small, has no transitive dependencies, and is already available in the local Maven cache. Hashes look like `$2a$<cost>$<53 chars>`, which contain no `;`, so the file format stays parseable.
- **Abstraction** in a new package `business.security`:
  - `interface PasswordHasher { String hash(String plain); boolean matches(String plain, String stored); }`
  - `BCryptPasswordHasher(int logRounds)` uses `BCrypt.hashpw(plain, BCrypt.gensalt(logRounds))` and `BCrypt.checkpw`.
  - `matches` returns `false`, instead of throwing, when `stored` is not a BCrypt hash. `checkpw` throws `IllegalArgumentException` (or `StringIndexOutOfBoundsException` for very short values) for such values, and both are caught.
  - `App` uses the library default cost (`BCryptPasswordHasher.DEFAULT_LOG_ROUNDS = 10`).
- **Injection.** `UserManager(UserFileRepository, PasswordHasher)`, wired in `App`. No default hasher is created inside `UserManager`, consistent with the component-wiring requirement.
- **Where hashing happens.**
  - `create`: validate all fields → uniqueness checks → `hasher.hash(password)` → store.
  - `update` with `PASSWORD`: validate → `hash` → set.
  - `authenticate`: `hasher.matches(plain, user.getPassword())`.
  - Error messages are unchanged.
  - `User.password` now holds the hash. The field name is kept to avoid churn, and its Javadoc says it holds a hash.
- **Tests.** A shared test helper `TestSupport.HASHER = new BCryptPasswordHasher(4)`, the minimum cost, keeps the suite fast. Assertions on stored passwords change from plaintext equality to `HASHER.matches(...)` plus "plaintext absent from file". `UserFileRepositoryTest` is unaffected, since the repository stores whatever string it is given.
- **Executable JAR keeps working.** jBCrypt is the first runtime dependency. A plain `maven-jar-plugin` JAR would fail with `NoClassDefFoundError` under `java -jar`, which the packaged-JAR check showed. `maven-shade-plugin` 3.6.0 is therefore bound to `package` with a `ManifestResourceTransformer` (same Main-Class), so `target/amusement-park-1.0-SNAPSHOT.jar` contains the dependency and the existing `application-bootstrap` "Executable JAR" requirement still holds.
- **Legacy files.** Documented behaviour: plaintext values never authenticate, so such users cannot be deleted or updated until re-created. This follows the instruction not to invent a migration.
- **UML.** Add `PasswordHasher` (interface) and `BCryptPasswordHasher` (attribute `logRounds : int`), the realisation, and `UserManager x--> PasswordHasher : passwordHasher`.

## Risks / Trade-offs

- [BCrypt is slow by design] → the production cost is 10 (tens of milliseconds per hash). Tests use cost 4.
- [Old data no longer authenticates] → explicit in the specs and here, as requested.
