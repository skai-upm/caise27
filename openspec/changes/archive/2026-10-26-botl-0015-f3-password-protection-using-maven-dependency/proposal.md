# Proposal

## Why

User passwords are stored in plaintext in `data/users.txt`, so anyone with access to the file can read them. Passwords must be protected with a standard one-way hash, which is also the project's first external library managed through Maven.

## What Changes

- Add the BCrypt library (`org.mindrot:jbcrypt`) as a Maven dependency and package it inside the executable JAR so `java -jar target/parque.jar` keeps working.
- Passwords are stored only as BCrypt hashes; the E1 password format rules are still checked on the plaintext before hashing.
- `USER DELETE` and `USER UPDATE` verify the supplied password against the stored hash; changing the password stores a new hash.
- Existing users stored with plaintext passwords are migrated to hashes when the users are loaded.
- Hashing is a business service behind a `PasswordHasher` abstraction injected into the user controller at startup.

## Capabilities

### New Capabilities
- `password-security`: Hashed password storage with BCrypt, verification, format rules before hashing, and migration of plaintext passwords.

### Modified Capabilities
- `user-persistence`: the password column of the users file holds the BCrypt hash.
- `project-build`: the executable JAR includes its runtime dependencies.

## Impact

- `pom.xml` (dependency + shade plugin); new `business.security` package (`PasswordHasher`, `BCryptPasswordHasher`); `UserManager` (hashing, verification, migration); `Bootstrap` and tests; UML.
