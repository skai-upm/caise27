# Tasks

## 1. Dependency and packaging

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin to `pom.xml`; verify `mvn package` produces a self-contained `target/amusement-park.jar` that runs from an empty directory

## 2. Protection

- [x] 2.1 Add `PasswordEncoder`/`BCryptPasswordEncoder`, inject it into `UserService`; hash on create and password change, verify with `matches` in authentication; verify unit tests (file has no plain password, `$2a$` prefix, wrong password and hash-as-password rejected, change, restart)
- [x] 2.2 Update the compliance test (jBCrypt allowed as the only runtime dependency) and tests comparing stored passwords; run `mvn clean package` and verify all tests pass
