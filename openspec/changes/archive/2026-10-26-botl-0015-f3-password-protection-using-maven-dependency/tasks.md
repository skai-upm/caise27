# Tasks

## 1. Build

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` (main class manifest) to `pom.xml`; verify `mvn package` produces `target/parque.jar` that runs `USER CREATE` via `java -jar`

## 2. Hashing

- [x] 2.1 Create `business.security.PasswordHasher` and `BCryptPasswordHasher`; verify with `BCryptPasswordHasherTest` (hash differs from plain, matches, wrong password rejected, isHash)
- [x] 2.2 Store `passwordHash` in `User`, inject the hasher into `UserManager` (hash on create/password update after format validation, verify on authenticate, migrate legacy plain-text on configure), wire it in `App`; verify with `UserManagerTest` (no plain text in file, password change persists new hash, format rules, legacy migration) and `mvn test`

## 3. Model

- [x] 3.1 Update `uml/model.puml` (security package, `User.passwordHash`, `UserManager` → `PasswordHasher`); verify `plantuml -checkonly` and the field cross-check pass
