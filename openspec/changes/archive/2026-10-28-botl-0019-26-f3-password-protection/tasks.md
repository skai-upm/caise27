# Tasks

## 1. Build

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin; verify `mvn clean package` succeeds and `unzip -l target/amusement-park.jar` contains `org/mindrot/jbcrypt/BCrypt.class`

## 2. Password protection

- [x] 2.1 Add `PasswordPolicy` (Delivery 1 rule) and `PasswordHasher`/`BCryptPasswordHasher`; verify with unit tests (valid/weak passwords, hash differs from plain, verification, new salt per hash)
- [x] 2.2 Add `passwordHash` to `User`, `AuthenticationException`, and `setUserPassword`/`authenticate`/hasher injection in `ParkController`; verify with controller tests (weak password rejected and unchanged, correct/wrong password, change produces new value)
- [x] 2.3 Store `dni;email;hash` in `TextUserRepository` (2-field lines still load); verify with a persistence test that the file has no plain password and authentication works after reload
- [x] 2.4 Update `uml/model.puml` and the dependency-scope check; verify `UmlModelTest` and the PlantUML syntax check pass

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and `java -jar target/amusement-park.jar` from another directory; verify it starts and BCrypt classes load
- [x] 3.2 Commit with the exact message `[F3-C05] Secure stored user passwords`; verify with `git log -1 --format=%s`
