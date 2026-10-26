# Tasks

## 1. Dependency and hasher

- [x] 1.1 Declare `org.mindrot:jbcrypt:0.4` in `pom.xml` and add `maven-shade-plugin` so `target/parque.jar` bundles it; verify `mvn package` and that the JAR contains `org/mindrot/jbcrypt/BCrypt.class`
- [x] 1.2 Create `PasswordHasher` and `BCryptPasswordHasher`; verify `BCryptPasswordHasherTest` (hash differs from plaintext and starts with `$2a$`, matches/does not match, `isHash`)

## 2. Hashed passwords

- [x] 2.1 Rename `User.password` to `passwordHash`; inject the hasher into `UserController.configure`, hash on create and PASSWORD update, verify with `matches`, migrate plaintext passwords on load; update `App.configure`/`TestPark`; verify `UserControllerTest` (stored value is a hash, delete/update with right/wrong password, change password persists new hash across restart, format rules before hashing, legacy file migrated) and the full suite

## 3. Model and end-to-end

- [x] 3.1 Update `uml/model.puml` (`passwordHash`, security package); verify PlantUML `-checkonly` and the UML cross-check script
- [x] 3.2 Run the regression script through the shaded JAR (outputs unchanged) and inspect `data/users.txt` for hashes only; verify a legacy plaintext file is migrated by the JAR
