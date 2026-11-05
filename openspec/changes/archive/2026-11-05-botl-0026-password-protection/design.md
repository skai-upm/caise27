# Design

## Context

`UserService` stores the plain password in `User` and compares with `equals`; `UserFileStorage` writes the field as is. The thin JAR has no dependencies on its classpath.

## Goals / Non-Goals

**Goals:** hashed-only storage, library-based verification, unchanged command syntax. **Non-Goals:** migrating old files, configurable cost per user.

## Decisions

- **`business.security.PasswordEncoder`** (interface: `encode`, `matches`) with **`BCryptPasswordEncoder`** (jBCrypt `hashpw(plain, gensalt(10))` / `checkpw`); `matches` returns false for values that are not BCrypt hashes instead of throwing. Injected into `UserService` by the composition root (a BCrypt default keeps the singleton usable before initialization).
- **`User.password` holds the protected value**; the field name stays, its meaning changes. `UserFileStorage` is unchanged (hash contains no `;`).
- **Validation before hashing**: create/update validate the plain text with `UserValidator.isValidPassword`, then encode.
- **Packaging**: `maven-shade-plugin` (main artifact replaced, `Main-Class` via `ManifestResourceTransformer`) keeps the `target/amusement-park.jar` name.
- **Tests** that compared stored plain passwords now use `matches`; file comparisons ignore the (salted) password field.

## Risks / Trade-offs

- [BCrypt cost slows tests] → cost 10 (~tens of ms) is acceptable for the test volume.
- [Existing plain-text `users.txt` from older versions stops authenticating] → accepted by the source (no migration required).
