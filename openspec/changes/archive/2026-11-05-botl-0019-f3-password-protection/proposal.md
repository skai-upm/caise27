# Proposal

## Why

Milestone F3-C05 requires that user passwords are never stored in plain text. Passwords must be protected with BCrypt through a Maven-added library (jBCrypt 0.4), and operations that authenticate (such as `USER DELETE` and `USER UPDATE`) must verify the entered password against the stored BCrypt value. Today the project has no hashing capability, and its Delivery 1 restriction checks forbid any runtime library and any encryption mechanism.

## What Changes

- Add the Maven dependency `org.mindrot:jbcrypt:0.4`. Bundle it into the executable JAR with `maven-shade-plugin`, so `java -jar target/amusement-park.jar` keeps running standalone.
- Add the business port `PasswordHasher` (protect a plain password, verify a plain password against a protected value) and its implementation `BCryptPasswordHasher` (jBCrypt, random salt per call). Central management gets the hasher by injection at initialization and exposes `protectPassword` and `verifyPassword`. Changing a password always produces a new protected value.
- Narrow the Delivery 1 restriction (**MODIFIED** `application-runtime`): jBCrypt is now the only permitted runtime library and the only encryption mechanism, used solely for password protection. Every other dependency stays test-scoped, and regular expressions and JDK crypto APIs remain forbidden.

### Deferred (not silently resolved)
- The user model, `USER CREATE/DELETE/UPDATE`, user persistence and the Delivery 1 password validation rules aren't in the code base yet (batch ordering: they come from later Delivery 1 milestones). The requirements below bind those milestones: they must validate first, store only `protectPassword(...)` results, and authenticate with `verifyPassword(...)`. Until then the protection is verified at component level. Migrating old plain-text files is not required.

## Capabilities

### New Capabilities
- `password-protection`: How user passwords are protected with BCrypt before storage and how authenticated operations verify them.

### Modified Capabilities
- `application-runtime`: the Delivery 1 restriction allows jBCrypt as the single runtime library or encryption mechanism.

## Impact

- `pom.xml` (dependency, shade plugin).
- New `business.PasswordHasher` and `business.BCryptPasswordHasher`.
- `ParkController` injection.
- `App` wiring.
- `TechnicalRestrictionsTest`, new tests and the UML model.
