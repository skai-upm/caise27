# Proposal

## Why

Milestone F3-C05 requires user passwords to be protected with BCrypt (a basic library such as jBCrypt added through Maven): the Delivery 1 password rules are applied before storing, only the BCrypt value is persisted, password-checked operations verify against the stored value with the library, and changing a password generates and persists a new protected value.

## What Changes

- Add the `org.mindrot:jbcrypt:0.4` dependency and package it inside the executable JAR so `java -jar target/amusement-park.jar` keeps working without extra files.
- Add a password-hashing abstraction in the business layer with a BCrypt implementation, injected into the park controller at initialization.
- Add the Delivery 1 password rule check (at least one uppercase letter, one lowercase letter, one digit and one symbol) applied before any password is protected and stored.
- Users store only the BCrypt value; the park controller offers setting/changing a user's password (validate → hash → store) and authenticating a user by e-mail and password.
- The user file stores `dni;email;bcryptHash`; files from earlier versions (`dni;email`) still load as users without a password (no migration).
- Relax the "no external libraries" restriction exactly for this dependency; update the UML model with the new user attribute.
- Commit with the exact message `[F3-C05] Secure stored user passwords`.

## Capabilities

### New Capabilities
- `password-protection`: Password rules, BCrypt protection, verification and persistence of protected passwords.

### Modified Capabilities
- `application-execution`: the technical-restrictions requirement allows the jBCrypt runtime dependency (Delivery 3) bundled in the executable JAR.

## Impact

- `pom.xml` (dependency, shade plugin), `User`, `ParkController`, new `business.security` package, user repository format, UML, tests.
- **Flagged ambiguities / ordering conflicts:**
  - `USER CREATE`, `USER DELETE` and `USER UPDATE` (F1-C03/C05/C06) are applied later in this batch. This change provides the protected-password model, storage and verification operations those commands must use; no command exposes them yet.
  - The full Delivery 1 user validation (DNI, e-mail, card) is F1-C02; only the password rule is implemented now because this milestone requires it before storing passwords. "Symbol" is interpreted as any character that is not a letter, digit or whitespace.
  - BCrypt cost factor is not specified; the library default (10) is used.
