# Design

## Context

`UserService` stores the password string as given and authenticates with `user.getPassword().equals(password)`; `UserRepository` writes it as field 5. `ApplicationContext` is the composition root that creates and injects components. `pom.xml` has only test dependencies, so the plain JAR is executable without a classpath.

## Goals / Non-Goals

**Goals:** hashes at rest, verification against hashes, external library via Maven, unchanged command contract.

**Non-Goals:** other authentication schemes, migration of plaintext files, password reset.

## Decisions

- **Library**: `org.mindrot:jbcrypt:0.4` — small, dependency-free, standard BCrypt (`hashpw`, `gensalt`, `checkpw`).
- **Abstraction**: `interface PasswordHasher { String hash(String plain); boolean matches(String plain, String hash); }` with `BCryptPasswordHasher(int logRounds)` (default 10). `matches` returns false (instead of throwing) when the stored value is not a BCrypt hash. The library is used only in this class, so it can be replaced.
- **Injection**: `UserService(UserRepository, PasswordHasher)` main constructor; `ApplicationContext` creates `new BCryptPasswordHasher()` and injects it. The one-argument constructor kept for tests delegates to a BCrypt hasher.
- **Where hashing happens**: in `UserService.create` after all E1 validations, and in `update` for `PASSWORD` after validation; `authenticate` uses `hasher.matches`. `User` keeps a single `password` field that now holds the hash — no API change for repositories.
- **Packaging**: `maven-shade-plugin` 3.6.0 bound to `package`, with a `ManifestResourceTransformer` (`Main-Class: com.parque.App`), replacing the main artifact so the JAR name stays `parque-1.0-SNAPSHOT.jar`; signature files of dependencies are excluded.
- **Tests**: the user-file assertions that expected plaintext now assert a hash that verifies the password; a test helper uses log rounds 4 to keep the suite fast.

## Risks / Trade-offs

- [Hashing cost ≈ tens of ms per create/update at factor 10] → acceptable for a command-line application.
- [Old plaintext data stops authenticating] → required by "no automatic migration"; documented.
