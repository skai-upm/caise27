# Proposal

## Why

Milestone F3-C05: passwords are stored and compared in plain text. They must be protected with BCrypt (added through Maven), validated with the Delivery 1 rules before being protected, stored only as BCrypt values, and verified with the library for authenticated operations.

## What Changes

- Add the Maven dependency `org.mindrot:jbcrypt:0.4` and package it inside the executable JAR so `java -jar target/nombre-aplicacion.jar` keeps working without extra configuration.
- Add a password-protection component (hash / verify) backed by jBCrypt, injected into the user controller at initialization.
- USER CREATE and USER UPDATE (PASSWORD) validate the plain password with the Delivery 1 rules, then store a newly generated BCrypt value.
- USER DELETE and USER UPDATE verify the entered password against the stored BCrypt value.
- The users file stores only the BCrypt value. Files from earlier versions are not migrated.
- Commit with the exact message `[F3-C05] Secure stored user passwords`.

## Capabilities

### New Capabilities
- `password-protection`: BCrypt protection and verification of user passwords.

### Modified Capabilities
<!-- none: user commands keep their syntax and responses -->

## Impact

- `pom.xml` (dependency + `maven-shade-plugin` producing the self-contained `nombre-aplicacion.jar`), new `business.security.PasswordHasher` / `BCryptPasswordHasher`, `UserController`, `ApplicationInitializer`; tests that inspected plain passwords now check verification.
- The Delivery 1 "standard library only" restriction is superseded for this dependency by Delivery 3; the source-restriction test allows `org.mindrot.jbcrypt`.
- Ambiguity flagged: a work factor of 10 is used (jBCrypt default); in-memory test configurations use 4 to keep tests fast.
