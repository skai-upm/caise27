# Proposal

## Why

User passwords are stored in plain text in `data/users.txt`, so anyone reading the file learns every credential. Delivery 3 requires protecting them with BCrypt, introduced as an external Maven dependency.

## What Changes

- Add the BCrypt library `org.mindrot:jbcrypt:0.4` to `pom.xml`; package an executable JAR that bundles it (`maven-shade-plugin`), so `java -jar target/parque.jar` keeps working.
- Passwords are validated with the E1 format rules first, then stored only as BCrypt hashes (on `USER CREATE` and on `USER UPDATE …;PASSWORD;…`).
- `USER DELETE` and `USER UPDATE` verify the supplied password against the stored hash.
- Existing user files written by earlier versions (plain-text passwords) are migrated: at load time any non-hash password is hashed and the file is rewritten.
- Hashing is a business service (`PasswordHasher` abstraction, `BCryptPasswordHasher` implementation) injected into `UserManager` at startup.
- The stale `project-build` requirement "No domain functionality" (baseline-only rule, contradicted by every later epic) is removed.
- Required commit title: `[F3-C05] Secure stored user passwords`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `user-management`: the password is kept only as a hash; authentication verifies against it.
- `user-persistence`: the user file stores the password hash; legacy plain-text passwords are migrated on load.
- `project-build`: BCrypt dependency declared and bundled in the executable JAR; obsolete baseline requirement removed.

## Impact

- `pom.xml` (dependency + shade plugin); new `business.security` package (`PasswordHasher`, `BCryptPasswordHasher`); `User` holds `passwordHash`; `UserManager.configure` gains the hasher; `App` wires it; tests use a low BCrypt cost for speed.
