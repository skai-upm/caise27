# Design

## Context

`User(dni, email)`; `ParkController` (singleton) with injected `ParkRepositories`; `TextUserRepository` stores `dni;email`. No password exists yet; the USER commands arrive later in this batch. The JAR is a plain `maven-jar-plugin` JAR (no dependencies inside).

## Decisions

- **Dependency**: `org.mindrot:jbcrypt:0.4` (compile scope). **`maven-shade-plugin` 3.6.0** at `package` builds a self-contained `target/amusement-park.jar` (main class kept by the existing jar manifest; `createDependencyReducedPom=false`). Alternative: `Class-Path` + copied `lib/` — rejected, the submitted JAR must run alone.
- **Port** `business.security.PasswordHasher` (`hash(plain)`, `matches(plain, hash)`) with `BCryptPasswordHasher` (`BCrypt.hashpw(plain, BCrypt.gensalt())`, `BCrypt.checkpw`). Injected with `ParkController.configure(PasswordHasher)`; defaults to BCrypt so the controller never stores plain text.
- **`PasswordPolicy.validate(plain)`** (business): character loop with `Character.isUpperCase/isLowerCase/isDigit`; symbol = not letter/digit/whitespace; throws `InvalidValueException`.
- **`User`** gains encapsulated `passwordHash` (nullable), `hasPassword()`, package-free setter `setPasswordHash`. No plain text field anywhere.
- **Controller**: `setUserPassword(email, plain)` → validate → hash → store; `authenticate(email, plain)` → throws new `AuthenticationException extends ParkException` when user unknown, no hash, or mismatch.
- **Persistence**: `TextUserRepository` writes `dni;email;hash` (empty hash field when none) and reads 2 or 3 fields.
- **UML**: add `- passwordHash : String` to `User`.
