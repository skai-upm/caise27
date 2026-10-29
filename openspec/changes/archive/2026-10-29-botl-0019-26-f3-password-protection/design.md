# Design

## Context

`User(email, dni)` has no password; `TextFileUserRepository` writes `email;dni`. The JAR has no dependencies today.

## Goals / Non-Goals

**Goals:** BCrypt dependency, protected storage, verification API, persistence of hashes. **Non-Goals:** USER commands, Delivery 1 password rules (later proposals), migrating old files.

## Decisions

- **Dependency** `org.mindrot:jbcrypt:0.4` (compile scope). **Packaging**: `maven-shade-plugin` 3.6.0 bound to `package`, replacing the main JAR (same `amusement-park.jar`, same `Main-Class`).
- **`business.security.PasswordProtector`**: `protect(String plain)` → `BCrypt.hashpw(plain, BCrypt.gensalt())`; `matches(String plain, String hash)` → `BCrypt.checkpw`, false for null/malformed hashes.
- **`User`**: private `passwordHash` (nullable) with `getPasswordHash()` and a public `setPasswordHash(String)` that only accepts already protected values (used by central management and reconstruction); equality unchanged.
- **`ParkController`**: holds a `PasswordProtector` (created with the controller); `setPassword(email, plain)`, `verifyPassword(email, plain)` throwing `OperationNotAllowedException("invalid password ...")` on mismatch or missing password.
- **Persistence**: users line `email;dni;hash` (empty field when no password); 2-field lines still accepted.

## Risks / Trade-offs

- [Shade plugin needs network on first build] → artifacts are fetched once into the local repository.
- [`setPasswordHash` is public] → only a hash can be set through it; plain passwords only go through `ParkController.setPassword`.
