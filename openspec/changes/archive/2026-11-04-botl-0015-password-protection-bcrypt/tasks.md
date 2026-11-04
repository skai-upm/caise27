# Tasks

## 1. Dependency and abstraction

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` to `pom.xml` and create `business.security.PasswordHasher` and `BCryptPasswordHasher` (configurable cost, `matches` false for non-hash values); verify with `BCryptPasswordHasherTest` (hash differs from plaintext and between calls, matches correct/incorrect, plaintext stored value returns false)

## 2. Hashed users

- [x] 2.1 Inject the hasher into `UserManager` (hash on create and on PASSWORD update after validation, verify on authenticate) and wire it in `App`; add `TestSupport.HASHER` and update every test that builds a `UserManager` or checks stored passwords; verify `mvn clean test` passes
- [x] 2.2 Add tests for the `password-protection` scenarios (plaintext absent from file, authenticated delete/update, rotation across restart with a new hash, invalid new password, legacy plaintext entry rejected without exception); verify they pass

## 3. Model and integration

- [x] 3.1 Update `uml/model.puml` with the security classes and the `passwordHasher` association; verify PlantUML `-syntax`, `UmlModelTest` and the label/field check
- [x] 3.3 Bundle the runtime dependency into the executable JAR (`maven-shade-plugin` with the same Main-Class); verify `java -jar target/amusement-park-1.0-SNAPSHOT.jar` creates a user without `NoClassDefFoundError`
- [x] 3.2 Run `mvn clean package` and the JAR: create a user, inspect `users.txt` for a `$2a$10$` hash, then delete with wrong and right passwords in a second run
