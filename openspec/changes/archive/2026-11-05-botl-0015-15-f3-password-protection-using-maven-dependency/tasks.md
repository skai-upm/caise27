# Tasks

## 1. Dependency and hasher

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` to `pom.xml` with `copy-dependencies` to `target/lib` and a manifest `Class-Path`, and implement `PasswordHasher`/`BCryptPasswordHasher`; verify with `BCryptPasswordHasherTest` (hash differs from plaintext and starts with `$2a$`, two hashes of the same password differ, matches true/false, non-BCrypt stored value → false)

## 2. User manager

- [x] 2.1 Inject the hasher into `UserManager.configure`, hash on create and on PASSWORD update after E1 validation, verify credentials with `matches`, wire `BCryptPasswordHasher` in `ApplicationContext`; adapt tests; verify `UserManagerTest` (file has no plaintext, wrong password delete/update rejected, password change persists a new hash and works after restart, invalid password not stored, legacy plaintext record does not authenticate) and `mvn test`

## 3. Regression

- [x] 3.1 Run `mvn package` and the E1/E2 script with `java -jar target/<jar>`: output identical to the previous JAR and data files identical except the password column, which holds BCrypt hashes; run the layer check and update `uml/model.puml` (PasswordHasher, BCryptPasswordHasher) with PlantUML `-checkonly` and the code cross-check
