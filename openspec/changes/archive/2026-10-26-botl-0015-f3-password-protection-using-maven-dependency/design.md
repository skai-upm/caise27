# Design

## Context

`User.password` holds plain text; `User.checkPassword` compares strings; `UserManager` (singleton, repository injected) authenticates and updates. Files from earlier runs contain plain-text passwords. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- No plain-text password at rest; one injected hashing service; seamless upgrade of existing data.

**Non-Goals:**
- Hashing other fields (phone encryption arrives in a later delivery if requested), password policies beyond E1.

## Decisions

- **Library `org.mindrot:jbcrypt:0.4`** (tiny, no transitive deps, already in the local Maven cache). Alternative `at.favre.lib:bcrypt` — heavier API, not needed.
- **Fat JAR with `maven-shade-plugin` 3.6.0**: shades dependencies into `target/parque.jar` and keeps `Main-Class` via `ManifestResourceTransformer`; `finalName` unchanged.
- **`business.security.PasswordHasher`** interface `hash(String)`, `matches(String plain, String hash)`, `isHash(String)`; **`BCryptPasswordHasher(int logRounds)`** implementation (production 10, tests 4 for speed). Injected via `UserManager.configure(UserRepository, PasswordHasher)` — no internal instantiation (consistent with botl-0013).
- **`User`**: field `passwordHash` (getter/setter); `checkPassword` removed — verification lives in `UserManager.authenticate` through the hasher, keeping the model free of the library.
- **Migration** in `UserManager.configure`: for each loaded user whose stored value fails `isHash` (BCrypt prefix `$2a$`/`$2b$`/`$2y$` + 60 chars), replace it by `hash(value)`; if any changed, save once.
- **File format** unchanged (7 fields); BCrypt hashes never contain `;`.

## Risks / Trade-offs

- [BCrypt cost slows tests] → tests inject a hasher with 4 log rounds.
- [Legacy value that happens to look like a hash] → only possible if a user chose a 60-char `$2a$…` password; acceptable.
