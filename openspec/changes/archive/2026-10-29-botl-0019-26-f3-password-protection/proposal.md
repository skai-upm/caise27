# Proposal

## Why

Milestone F3-C05 requires user passwords to be stored only as BCrypt-protected values, using a BCrypt library added through Maven (for example `org.mindrot:jbcrypt:0.4`), and operations requiring authentication to verify the entered password against the stored value with that library; a changed password gets a new protected value.

## What Changes

- Add the Maven dependency `org.mindrot:jbcrypt:0.4` and package it inside the executable JAR so `java -jar target/amusement-park.jar` keeps working without extra configuration.
- Users can hold a protected password: central management offers setting/changing a user's password (a new BCrypt value is generated each time) and verifying an entered password against the stored value; failures are reported as business exceptions.
- The users file stores only the BCrypt value (`email;dni;hash`), never the original password; files from before this change (`email;dni`) are still read.
- The "Java standard library only" build rule is relaxed for this one dependency, as Delivery 3 explicitly allows.

## Capabilities

### New Capabilities
- `password-protection`: protected storage and verification of user passwords.

### Modified Capabilities
- `project-build`: runtime dependencies limited to the Java standard library plus the BCrypt library, packaged in the executable JAR.

## Impact

- `pom.xml` (dependency, shade plugin), `business.security.PasswordProtector`, `User`, `ParkController`, `TextFileUserRepository`, tests, UML.

## Ambiguities and unmet prerequisites (flagged)

- **USER CREATE / DELETE / UPDATE** and the Delivery 1 password validation rules are delivered by later proposals in this batch. This change provides the protection and verification operations those commands must use; until then no command sets or checks a password, and users registered programmatically may have no password.
- Packaging the dependency into the JAR is done with `maven-shade-plugin` (the JAR keeps its name); the source does not prescribe a mechanism.
- BCrypt work factor: the library default (10) is used.
