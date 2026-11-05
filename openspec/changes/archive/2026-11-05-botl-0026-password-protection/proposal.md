# Proposal

## Why

Milestone F3-C05: user passwords are stored in plain text in `users.txt`. Delivery 3 requires them to be protected with BCrypt through a Maven dependency (jBCrypt), keeping the Delivery 1 password rules before storing, verifying passwords against the stored value for authenticated operations, and generating a new protected value on password change.

## What Changes

- Add the `org.mindrot:jbcrypt:0.4` Maven dependency; package it inside the executable JAR so `java -jar` keeps working without extra configuration.
- `USER CREATE` validates the plain password with the existing rules, then stores only its BCrypt hash.
- `USER DELETE` and `USER UPDATE` verify the entered password against the stored hash with jBCrypt.
- `USER UPDATE ...;PASSWORD;<new>` validates the new password and stores a new hash.
- `users.txt` contains only BCrypt hashes. Migration of old plain-text files is not required.
- The Delivery 1 "standard library only" restriction is relaxed exactly for this mandated dependency.

## Capabilities

### New Capabilities
- `password-protection`: BCrypt storage and verification of user passwords.

### Modified Capabilities
- `project-initialization`: the runtime-dependency restriction now allows the mandated BCrypt library.
- `executable-application`: the executable JAR bundles its runtime dependency.

## Impact

- `pom.xml` (dependency + shade plugin); new `business.security` (`PasswordEncoder`, `BCryptPasswordEncoder`); `UserService`; tests that compared stored passwords.
