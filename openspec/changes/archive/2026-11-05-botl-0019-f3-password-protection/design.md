# Design

## Context

There is no user model yet. Central management has injected ports (`UserDirectory`, `ParkRepository`). `TechnicalRestrictionsTest` enforces "test-scoped dependencies only" and "no crypto". See proposal.md - Why.

## Decisions

- **Port `PasswordHasher`** (`String protect(String plain)`, `boolean verify(String plain, String stored)`) lives in business, and **`BCryptPasswordHasher`** implements it with `BCrypt.hashpw(plain, BCrypt.gensalt())` and `BCrypt.checkpw`. Invalid stored values (for which `checkpw` throws `IllegalArgumentException`) verify as `false`. The port keeps the library behind one class, so tests or future algorithms can swap it.
- **Injection**: `ParkController.setPasswordHasher(PasswordHasher)` is configured by `App` at initialization. `reset()` restores the BCrypt default so business code never runs without protection. `protectPassword` and `verifyPassword` delegate to it.
- **Packaging**: `maven-shade-plugin` 3.6.0 at the `package` phase replaces the main JAR with one that includes jBCrypt. It keeps the existing manifest `Main-Class` and doesn't create a dependency-reduced POM. Alternatives rejected: `Class-Path` plus copied dependencies (breaks "single JAR"), and assembly (more configuration).
- **Restriction checks** are updated: jBCrypt is allowed as a non-test dependency, and the `org.mindrot.jbcrypt` import is allowed only in `BCryptPasswordHasher`.
