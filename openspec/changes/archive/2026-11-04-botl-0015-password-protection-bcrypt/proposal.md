# Proposal

## Why

User passwords are written in plaintext to `users.txt`. Delivery 3 requires storing them as BCrypt hashes using an external library added through Maven, while keeping the E1 password rules and the authenticated `USER DELETE`/`USER UPDATE` behaviour.

## What Changes

- Add the `org.mindrot:jbcrypt:0.4` dependency to `pom.xml`.
- Configure `maven-shade-plugin` so the executable JAR includes the dependency and still runs with `java -jar`.
- New business abstraction `PasswordHasher` (`hash`, `matches`) with implementation `BCryptPasswordHasher`, injected into `UserManager` at startup.
- `USER CREATE` and `USER UPDATE ... PASSWORD`: validate the plaintext with the E1 rules, then store `hasher.hash(password)`.
- `USER DELETE` / `USER UPDATE` authentication uses `hasher.matches(supplied, storedHash)`.
- Stored values that are not BCrypt hashes (plaintext from older files) never authenticate and are not migrated; this is documented, and the command fails cleanly.
- Command syntax and replies are unchanged.

## Capabilities

### New Capabilities
- `password-protection`: BCrypt dependency, hashed storage, validation before hashing, authentication against hashes and legacy-record behaviour.

### Modified Capabilities
- `user-file-persistence`: "Text file format" stores the hash instead of the plaintext password.
- `user-management`: "Authenticated deletion" verifies against the stored hash.
- `application-bootstrap`: "Executable JAR packaging" includes runtime dependencies.
- `command-input-modes`: "Shared processing" — equivalent runs produce identical users files except for the salted password hashes (found during implementation: random salts make byte equality impossible).

## Impact

- `pom.xml` (dependency, shade plugin), new `PasswordHasher`/`BCryptPasswordHasher`, `UserManager` (initialize signature, hashing), `ParkSession` (wiring), tests, `uml/model.puml`.
- Existing `users.txt` files with plaintext passwords keep loading, but those users cannot authenticate until re-created (no migration, by requirement).
