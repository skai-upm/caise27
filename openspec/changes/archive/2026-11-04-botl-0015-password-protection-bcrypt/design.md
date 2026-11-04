# Design

## Context

`UserManager` validates passwords with `UserValidator.isValidPassword` and compares `user.getPassword().equals(password)` in `authenticate`. `User.password` is persisted verbatim by `UserFileStorage`. Controllers are Singletons wired by `ParkSession`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Hashing behind a business abstraction; library usage confined to one class; same command behaviour.

**Non-Goals:**
- Migrating plaintext files, other authentication schemes, password-reset features.

## Decisions

- **Library:** `org.mindrot:jbcrypt:0.4`, a small, widely used, dependency-free library offering `BCrypt.hashpw(pw, BCrypt.gensalt(rounds))` and `BCrypt.checkpw`.
- **Abstraction:** `business.service.PasswordHasher` (interface) and `BCryptPasswordHasher(int logRounds)`, default 10 rounds. `matches` returns `false` when the stored value is not a BCrypt hash (it does not start with `$2a$`/`$2b$`/`$2y$` or `checkpw` throws `IllegalArgumentException`), which gives the "legacy plaintext never authenticates" rule without crashing. Tests inject `new BCryptPasswordHasher(4)` to keep the suite fast.
- **Where hashing happens:** `UserManager.create`/`update(PASSWORD)` validate the plaintext first, then call `hasher.hash`; `authenticate` uses `hasher.matches`. `User` keeps a `password` field that now holds the hash, and the model is not aware of hashing. The file format is unchanged in shape; the hash contains no `;`.
- **Injection:** `UserManager.initialize(UserFileStorage, PasswordHasher)`; `ParkSession` passes `new BCryptPasswordHasher()`.
- **Packaging:** `maven-shade-plugin` (3.6.0) bound to `package`, with a `ManifestResourceTransformer` for `mainClass`, replacing the main artifact so `target/amusement-park-1.0-SNAPSHOT.jar` stays the runnable JAR.

## Risks / Trade-offs

- [Older plaintext data becomes non-authenticating] → Mandated (no automatic migration). Documented in the spec; such users can be deleted only by re-creating data.
- [BCrypt is slow by design] → 10 rounds (about 0.1 s per hash) in production; tests use 4 rounds.
