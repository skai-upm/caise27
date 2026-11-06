# Tasks

## 1. Build

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4`, the shade plugin and the test BCrypt cost to `pom.xml`, and verify `mvn package` produces a JAR containing `org/mindrot/jbcrypt/BCrypt.class` that starts with `java -jar`

## 2. Protection

- [x] 2.1 Add `PasswordHasher` and `BCryptPasswordHasher` and verify with unit tests: hash differs from plain and starts with `$2a$`, matches/does not match, non-BCrypt stored value does not match
- [x] 2.2 Inject the hasher into `UserService`; hash on create and password change, verify with BCrypt on authentication; adapt tests that asserted plain-text passwords and verify with tests for every `password-protection` scenario (including restart and legacy plain-text file)

## 3. Integration

- [x] 3.1 Run `mvn clean package` and verify all tests pass; run the JAR: create a user, inspect `users.txt` for the hash, restart and update/delete with correct and wrong passwords
- [x] 3.2 Run `openspec validate botl-0026-password-protection --strict` and verify the change is valid
