# Tasks

## 1. Build

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` (Main-Class `com.parque.App`) to `pom.xml`; verify `mvn package` produces an executable JAR containing `org/mindrot/jbcrypt/BCrypt.class`

## 2. Hashing

- [x] 2.1 Create `PasswordHasher` and `BCryptPasswordHasher` (non-hash stored values never match); verify `BCryptPasswordHasherTest` (hash ≠ plaintext, `$2` prefix, salted, matches/mismatches, legacy plaintext rejected)
- [x] 2.2 Inject the hasher into `UserService` (hash on create and password update after E1 validation, verify on authenticate) and wire it in `ApplicationContext`; update tests expecting plaintext; verify `PasswordProtectionTest` (file has no plaintext, delete/update with wrong password rejected, rotation survives restart, E1 rules) and full `mvn test`

## 3. Documentation and regression

- [x] 3.1 Update `uml/model.puml` (`PasswordHasher` interface, `BCryptPasswordHasher`, `UserService.hasher`) and verify `UmlModelTest`/`plantuml -checkonly`; run the F3 script with the shaded JAR (normal and `--fly`) and verify outputs identical to the baseline and the users file differs only in the hashed password field
