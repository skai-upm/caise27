# Proposal

## Why

Milestone F3-C05 stops storing user passwords in plain text: passwords are protected with BCrypt through a Maven-managed library, only the protected value is persisted, and operations that require the password verify it against the stored value with that library.

## What Changes

- Add the jBCrypt dependency (`org.mindrot:jbcrypt:0.4`) and package it inside the executable JAR so `java -jar` keeps working without extra configuration.
- USER CREATE and USER UPDATE (PASSWORD) validate the password with the Delivery 1 rules first, then store a newly generated BCrypt value.
- USER DELETE and USER UPDATE verify the entered password against the stored BCrypt value.
- The users file contains only BCrypt values, never the original password. Plain-text files from earlier versions are not migrated.
- The Delivery 1 restriction "no external libraries / no encryption" is relaxed exactly for this library, as Delivery 3 requires.
- Record the mandatory milestone commit `[F3-C05] Secure stored user passwords` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `user-management`: passwords protected with BCrypt and verified with it.
- `persistence`: the users file stores the BCrypt value instead of the password.
- `project-build`: the runtime-library restriction now allows the BCrypt library, bundled in the JAR.

## Impact

- `pom.xml` (dependency + shade plugin); new `business.service.PasswordHasher` / `BCryptPasswordHasher` injected into `UserService`; tests that compared plain passwords now verify BCrypt values; UML updated.
