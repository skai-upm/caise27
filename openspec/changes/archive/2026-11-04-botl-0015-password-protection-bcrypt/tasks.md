# Tasks

## 1. Dependency and packaging

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` (main class manifest) to `pom.xml`; verify `mvn package` produces a JAR that starts with `java -jar` and contains `org/mindrot/jbcrypt/BCrypt.class`

## 2. Hashing

- [x] 2.1 Add `PasswordHasher` and `BCryptPasswordHasher` (salted hash, verify, non-hash values never match); verify with `BCryptPasswordHasherTest`
- [x] 2.2 Inject the hasher into `UserManager` (validate then hash on create/password change, verify on authenticate) and wire it in `ParkSession`; verify with updated `UserManagerTest` (hash stored, new hash on change, weak passwords rejected, wrong password rejected, legacy plaintext record rejected)

## 3. Model and regression

- [x] 3.1 Update `uml/model.puml`; verify `UmlModelTest`, `LayeringTest` and the PlantUML `-syntax` check
- [x] 3.2 Run `mvn clean package` and the E1/E2 regression script: outputs identical to baseline (plus the HELP lines added earlier) and `users.txt` contains BCrypt hashes instead of plaintext
