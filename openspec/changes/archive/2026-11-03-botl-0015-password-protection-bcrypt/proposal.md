# Proposal

## Why

Passwords are stored in plaintext in `users.txt`. They must be stored as BCrypt hashes, using an external library added through Maven, while every authenticated operation keeps working.

## What Changes

- Declare `org.mindrot:jbcrypt:0.4` in `pom.xml` and bundle it into the executable JAR (`maven-shade-plugin`) so `java -jar` keeps working.
- New business abstraction `PasswordHasher` (`hash`, `matches`) implemented by `BCryptPasswordHasher`, injected into `UserController` at startup.
- `USER CREATE` and `USER UPDATE ...;PASSWORD;...` validate the plaintext password with the E1 rules first, then store a newly salted BCrypt hash; `USER DELETE`/`USER UPDATE` verify the supplied password against the stored hash.
- **BREAKING (data):** the password field of `users.txt` now contains a BCrypt hash. Pre-existing plaintext records are not migrated and can no longer authenticate (documented in design).

## Capabilities

### New Capabilities
- `password-protection`: hashed storage of passwords and verification against hashes.

### Modified Capabilities
- `user-file-persistence`: the password field of the user file holds a BCrypt hash.
- `domain-model-diagram`: the diagram shows the password hasher abstraction.

## Impact

- `pom.xml` (dependency + shade plugin), new `business.security` package, `UserController`, `App` wiring, tests that inspected plaintext passwords in files, `uml/model.puml`.
