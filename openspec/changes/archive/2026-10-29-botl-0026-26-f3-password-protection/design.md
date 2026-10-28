# Design

## Context

`UserService` stores `password` as given and compares with `equals` in `authenticate`; `UserFileStorage` writes the field verbatim. See proposal.md.

## Goals / Non-Goals

**Goals:** hashing in one place; standalone JAR.
**Non-Goals:** migrating old files; configurable work factor.

## Decisions

- **`business.security.PasswordHasher`** (final utility): `hash(String plain)` = `BCrypt.hashpw(plain, BCrypt.gensalt())`; `matches(String plain, String hash)` = `BCrypt.checkpw`, returning `false` for values that are not BCrypt hashes (`IllegalArgumentException`).
- **`UserService`**: `createUser` validates the plain password, then constructs the `User` with the hash; `updateUser(PASSWORD)` validates then stores a new hash; `authenticate` uses `PasswordHasher.matches`.
- **`User.password`** now holds the protected value (attribute name kept; documented).
- **Packaging**: `maven-shade-plugin` 3.6.0 bound to `package`, keeping `finalName` so the shaded JAR replaces `target/amusement-park.jar`; the manifest main class stays in the jar plugin configuration.

## Risks / Trade-offs

- [BCrypt is slow by design] → acceptable for interactive use; tests create few users each.
