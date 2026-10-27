# Tasks

## 1. Library and hasher

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin to `pom.xml`; verify `mvn package` produces a runnable `target/nombre-aplicacion.jar` containing `org/mindrot/jbcrypt/BCrypt.class`
- [x] 1.2 Create `PasswordHasher` and `BCryptPasswordHasher`; verify with `BCryptPasswordHasherTest` (hash differs from plain, `$2a$` prefix, matches/non-matches, malformed value)

## 2. Integration

- [x] 2.1 Hash on USER CREATE / PASSWORD update and verify with BCrypt on USER DELETE / UPDATE, injecting the hasher; update tests that inspected plain passwords; verify the password-protection scenarios in `PasswordProtectionTest` (including restart and file content)

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C05] Secure stored user passwords`; verify with `git log -1 --format=%s`
