# Proposal

## Why

User passwords are stored in plaintext in `data/users.txt`. Delivery 3 requires protecting them with BCrypt, added as an external library through Maven, while keeping the password rules from Delivery 1.

## What Changes

- Add the BCrypt library (`org.mindrot:jbcrypt`) as a Maven dependency.
- Passwords are validated with the E1 format rules and then stored only as BCrypt hashes (on `USER CREATE` and when changing the password with `USER UPDATE`).
- `USER DELETE` and `USER UPDATE` verify the supplied password against the stored hash.
- Plaintext passwords found in an existing users file are hashed when the file is loaded, so no plaintext remains after the next save.
- The executable JAR bundles its runtime dependency so `java -jar target/parque.jar` keeps working.
- **Spec cleanup:** the obsolete Delivery 0 requirement "No domain functionality yet" is removed from `project-build`.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `user-management`: passwords are stored as BCrypt hashes and verified against them.
- `project-build`: the BCrypt dependency is declared, the executable JAR includes runtime dependencies, and the obsolete "no domain functionality" requirement is removed.

## Impact

- `pom.xml` (dependency, shade plugin); new `PasswordHasher` in `business.security`; `UserController` (create/update/authenticate/load), `App` wiring, tests.
- Data format: the password column of `users.txt` now contains BCrypt hashes.
