# Proposal

## Why

User passwords are written in plaintext to `users.txt`. They must be stored as salted hashes, using an external library added through Maven, while user creation, update and deletion keep working with the same commands.

## What Changes

- Add the BCrypt library `org.mindrot:jbcrypt:0.4` to `pom.xml`.
- New business abstraction `PasswordHasher` (`hash`, `matches`) with the implementation `BCryptPasswordHasher`, injected into `UserManager` by `App`.
- `USER CREATE` and a password change through `USER UPDATE` validate the plaintext with the E1 rules, then store only the BCrypt hash.
- Authentication (`USER DELETE`, `USER UPDATE`) verifies the supplied password against the stored hash.
- Stored values that are not BCrypt hashes, such as plaintext from older files, never authenticate. There is no migration; this is documented.
- Command syntax and outputs are unchanged.

## Capabilities

### New Capabilities
- `password-protection`: BCrypt dependency, hashed storage, hash-based authentication, password rotation, behaviour for non-hash values.

### Modified Capabilities
- `user-file-persistence`: the password field of the users file holds the BCrypt hash.

## Impact

- `pom.xml` (new dependency).
- New package `business.security`. `UserManager` constructor (hasher injected), `App` wiring, and the tests that build user managers or inspect stored passwords.
- Existing `users.txt` files with plaintext passwords stop authenticating those users.
- Delivery milestone message: `[F3-C05] Secure stored user passwords`.
