# Proposal

## Why

Milestone F3-C05 requires user passwords to be stored only as BCrypt-protected values, using a BCrypt library added through Maven. Operations that need authentication, such as USER DELETE and USER UPDATE, must verify the entered password against the stored value, and a changed password must be protected again before it is stored. The password validation rules must run before any new password is protected. Because of the processing order of this batch, the user model, USER commands and user persistence do not exist yet. This change therefore delivers the password protection component, its library and packaging, so that those milestones store and verify passwords through it.

## What Changes

- Add the `org.mindrot:jbcrypt:0.4` dependency (the library suggested by the source) and package it inside the executable JAR, so that `java -jar target/amusement-park.jar` keeps working with no extra classpath.
- Add a password protection service in the business layer:
  - `protect(rawPassword)` applies the password validation policy and then returns a new BCrypt hash. The raw password never appears in the result.
  - `verify(rawPassword, storedHash)` checks an entered password against a stored BCrypt value.
- The hashing algorithm and the validation policy are injected dependencies. The default policy only rejects missing or blank passwords, and the Delivery 1 password rules are injected by the valid-users milestone.
- Wire the service in the application configuration, ready for the user milestones.
- Relax the "standard library only" runtime rule so that jBCrypt is the only permitted third-party runtime library.

### In scope
- Library, packaging, password service, injection, configuration, tests.

### Out of scope
- Storing users and running USER DELETE/UPDATE (user milestones). These must call `protect` before persisting a password and `verify` to authenticate.
- Migrating old user files (not required by the source).

## Capabilities

### New Capabilities
- `password-protection`: how user passwords are protected with BCrypt before storage and how entered passwords are verified.

### Modified Capabilities
- `application-execution`: the runtime-dependency rule now allows jBCrypt bundled in the executable JAR.

## Impact

- `pom.xml`: jbcrypt dependency and `maven-shade-plugin` (main class manifest, same final JAR name).
- New `es.upm.park.business.security` package (`PasswordHasher`, `BCryptPasswordHasher`, `PasswordPolicy`, `PasswordService`). `AppConfiguration` wires them.
- Tests, UML, README.
