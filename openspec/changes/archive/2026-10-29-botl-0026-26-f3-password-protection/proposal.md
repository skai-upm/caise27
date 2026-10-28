# Proposal

## Why

Milestone F3-C05: user passwords must no longer be stored in plain text. A BCrypt library added through Maven must protect stored passwords and verify them in authenticated operations.

## What Changes

- Add the Maven dependency `org.mindrot:jbcrypt:0.4` and package it inside the executable JAR (shade plugin) so `java -jar target/amusement-park.jar` keeps working without extra configuration.
- Validate the password with the Delivery 1 rules first, then store only its BCrypt hash (on `USER CREATE` and on `USER UPDATE ... PASSWORD`, which generates a new hash).
- `USER DELETE` and `USER UPDATE` verify the entered password against the stored hash with the library.
- `users.txt` contains only the BCrypt value in the password column.
- Commit with the mandatory message `[F3-C05] Secure stored user passwords`.

## Capabilities

### New Capabilities
- `password-protection`: BCrypt protection of stored passwords and verification in authenticated operations.

### Modified Capabilities
- None (other capabilities keep their observable behavior; credentials are still checked the same way from the user's point of view).

## Impact

- `pom.xml` (dependency + `maven-shade-plugin`), new `business.security.PasswordHasher`, `UserService`; tests that inspected stored plain passwords.
- Ambiguities flagged:
  - Migration of older plain-text `users.txt` files is not required; a stored value that is not a BCrypt hash never matches.
  - The BCrypt work factor is the library default (10).
