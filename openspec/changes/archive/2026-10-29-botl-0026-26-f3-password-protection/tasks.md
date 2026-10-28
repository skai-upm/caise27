# Tasks

## 1. BCrypt

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin to `pom.xml`; verify `mvn package` produces `target/amusement-park.jar` containing `org/mindrot/jbcrypt/BCrypt.class`
- [x] 1.2 Create `business.security.PasswordHasher` and use it in `UserService` (hash on create and password change after validation; verify on authenticate); verify unit tests for hashing, verification, wrong passwords, password change and non-hash values
- [x] 1.3 Update tests that inspected stored plain passwords to check BCrypt values; verify the full suite passes and a JAR run shows `users.txt` without the plain password and USER DELETE working after restart

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F3-C05] Secure stored user passwords`; verify with `git log -1 --format=%s`
