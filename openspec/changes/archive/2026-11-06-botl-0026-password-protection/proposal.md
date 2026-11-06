# Proposal

## Why

Milestone F3-C05 (Delivery 3, step 5) forbids storing user passwords in plain text: a BCrypt library added through Maven must protect them, and every operation that checks a password (`USER DELETE`, `USER UPDATE`) must verify it against the stored protected value. Today `users.txt` holds the passwords as typed.

## What Changes

- Add the jBCrypt dependency (`org.mindrot:jbcrypt:0.4`) and bundle it into the executable JAR so `java -jar` keeps working without a classpath.
- Validate a new password with the Delivery 1 rules first, then store only its BCrypt hash; the user file never contains the original password.
- Verify passwords for `USER DELETE` and `USER UPDATE` with BCrypt against the stored hash.
- Changing the password generates and persists a new hash.
- No migration of files written by earlier versions (a plain-text stored password simply never verifies).
- The Delivery 1 "standard library only" restriction now admits this single library mandated by Delivery 3.

### In scope
- Source section 8.

### Out of scope
- Migration of legacy user files; other secrets (card numbers are unchanged).

## Capabilities

### New Capabilities
- `password-protection`: BCrypt protection of stored passwords and verification of authenticated operations.

### Modified Capabilities
- `user-persistence`: the password field of the user file holds the BCrypt hash.
- `delivery-1-integration`: the technical restrictions allow the BCrypt library required by Delivery 3.

## Impact

- `pom.xml` (dependency, shade plugin, BCrypt cost for tests); new `PasswordHasher`/`BCryptPasswordHasher` injected into `UserService`; tests that asserted plain-text passwords.
