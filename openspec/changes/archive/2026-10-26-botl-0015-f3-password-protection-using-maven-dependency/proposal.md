# Proposal

## Why

User passwords are written to `data/users.txt` in plaintext, so anyone with access to the file can read them. Delivery 3 requires storing only password hashes, computed with BCrypt from an external library added through Maven — the project's first runtime dependency.

## What Changes

- Add a BCrypt library (`org.mindrot:jbcrypt:0.4`) as a Maven dependency in `pom.xml`.
- Passwords are validated with the existing Delivery 1 format rules and then stored only as BCrypt hashes; plaintext passwords are never written to disk.
- `USER DELETE` and `USER UPDATE` verify the supplied password against the stored hash.
- Changing the password with `USER UPDATE …;PASSWORD;<new>` validates the new value, hashes it and persists the new hash.
- Existing users stored with plaintext passwords (data from earlier deliveries) are converted to hashes when loaded, so they keep working with their current password.
- The executable JAR bundles the runtime dependency so `java -jar target/parque.jar` keeps working.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `user-management`: passwords are stored as BCrypt hashes, credential checks compare against the hash, password changes persist a new hash, and legacy plaintext passwords are migrated on load.
- `project-build`: the build declares the BCrypt dependency and the executable JAR includes runtime dependencies.

## Impact

- `pom.xml` (dependency + shade plugin producing `target/parque.jar`); new `business.security` package (`PasswordHasher` interface, `BCryptPasswordHasher`); `UserController` receives the hasher through `configure`; `User` stores a password hash; `App.configure` injects the hasher (tests use a low BCrypt cost for speed).
- `uml/model.puml` updated. Tests for hashing, verification, password change and migration.
