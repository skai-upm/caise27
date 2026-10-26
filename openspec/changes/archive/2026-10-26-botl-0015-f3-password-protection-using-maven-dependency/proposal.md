# Proposal

## Why

User passwords are stored in plaintext in `data/users.txt`. Delivery 3 requires protecting them with BCrypt hashes provided by an external library declared as a Maven dependency, while keeping the Delivery 1 password format rules.

## What Changes

- Add the BCrypt library (`org.mindrot:jbcrypt:0.4`) as a Maven dependency.
- New passwords (on `USER CREATE` and when changed with `USER UPDATE ...;PASSWORD;...`) are validated with the E1 rules and then stored as BCrypt hashes, never in plaintext.
- `USER DELETE` and `USER UPDATE` verify the supplied password against the stored hash.
- Users loaded with a legacy plaintext password are migrated to a hash immediately, so no plaintext password remains on disk.
- Password hashing is injected into the user controller through a `PasswordEncoder` abstraction.
- The executable JAR bundles the dependency so `java -jar target/parque.jar` keeps working.
- **BREAKING (data format)**: the password column of `users.txt` now contains a BCrypt hash.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `user-management`: passwords are stored as hashes and verified against them; file format scenario and stored-values scenario updated.
- `project-build`: BCrypt dependency declared; executable JAR includes dependencies; the obsolete "No domain functionality yet" requirement is removed.
- `uml-model`: the model shows the password encoder abstraction.

## Impact

- `pom.xml` (dependency + shade plugin), new `es.upm.parque.business.security` (`PasswordEncoder`, `BCryptPasswordEncoder`), `UserController`, `App`, tests, `uml/model.puml`.
