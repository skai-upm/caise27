# Proposal

## Why

User passwords are stored in plaintext in `users.txt`. They must be stored as BCrypt hashes produced by an external library added through Maven, while authentication for `USER DELETE` and `USER UPDATE` keeps working and the E1 password rules still apply.

## What Changes

- Declare the BCrypt library `org.mindrot:jbcrypt:0.4` as a Maven dependency.
- On `USER CREATE` and on `USER UPDATE ...;PASSWORD;...`, validate the plaintext password with the E1 rules, then store only its BCrypt hash; each change produces a new hash (new salt).
- `USER DELETE` and `USER UPDATE` verify the supplied current password against the stored hash.
- Hashing is behind a `PasswordHasher` abstraction injected into the user manager at startup.
- No automatic migration: a stored value that is not a BCrypt hash (a record written before this change) never authenticates.
- Commands and messages are unchanged; the password column of `users.txt` now holds the hash.

The source suggests change ID `protect-passwords-using-bcrypt`; this batch uses the BOTL ID `botl-0015-15-f3-password-protection-using-maven-dependency`. Mandatory milestone message: `[F3-C05] Secure stored user passwords`.

## Capabilities

### New Capabilities
- `password-storage`: BCrypt hashing of stored passwords and verification of supplied passwords.

### Modified Capabilities
- `user-file-persistence`: the stored password field is the BCrypt hash, not the plaintext.
- `user-model`: the user holds the password hash rather than the plaintext password.

## Impact

- `pom.xml` dependency; new `business.PasswordHasher` and `business.BCryptPasswordHasher`; `UserManager` hashes and verifies; `ApplicationContext` injects the hasher.
- Existing data files with plaintext passwords no longer authenticate (documented, no migration).
