# Proposal

## Why

Passwords are stored in plaintext in `users.txt` and compared as plain strings. Delivery 3 requires storing only BCrypt hashes, verifying authentication against them, and introducing that protection through an external library declared in Maven.

## What Changes

- Declare the BCrypt library `org.mindrot:jbcrypt` in `pom.xml`.
- Package the executable JAR with its runtime dependency (`maven-shade-plugin`), so `java -jar target/parque-1.0-SNAPSHOT.jar` keeps working now that the application has a third-party dependency.
- New `PasswordHasher` abstraction (`hash`, `matches`) in the business layer with the `BCryptPasswordHasher` implementation, created at startup and injected into `UserService`.
- `USER CREATE` validates the password with the E1 rules, then stores its BCrypt hash; `USER UPDATE;…;PASSWORD;…` validates the new password and stores a new hash.
- `USER DELETE`, `USER UPDATE` (and every operation that authenticates by email + password) verify the supplied password against the stored hash.
- The user file keeps its 7-field layout; the password field now holds the hash.

## Capabilities

### New Capabilities
- `password-protection`: BCrypt dependency, hashing of new/changed passwords, verification against hashes, E1 rules before hashing.

### Modified Capabilities
- `user-model`: "User data" — the user holds the password's hash, not the plaintext.
- `user-file-persistence`: "User file format" — the password field contains the BCrypt hash.

## Impact

- **Build**: `pom.xml` gains `org.mindrot:jbcrypt:0.4` (compile scope) and `maven-shade-plugin` (executable shaded JAR with `Main-Class: com.parque.App`).
- **Code**: new `business.service.PasswordHasher`, `BCryptPasswordHasher`; `UserService` (hash on create/update, verify on authenticate); `ApplicationContext` injects the hasher.
- **Data**: new and changed passwords are stored as `$2a$…` hashes. Responses of all commands are unchanged.
- **Mandatory milestone message**: `[F3-C05] Secure stored user passwords`.
- **Open questions / assumptions**:
  - No automatic migration of older files (as the source states): a stored value that is not a BCrypt hash never authenticates; such users would need to be recreated. This is reported, not silently converted.
  - BCrypt work factor 10 (library default) in production; the hasher accepts another factor so tests can run faster without changing behaviour.
