# Tasks

## 1. Dependency and packaging

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` (main class preserved) to `pom.xml`; verify `mvn package` builds a JAR containing `org/mindrot/jbcrypt/BCrypt.class`

## 2. Hashing abstraction

- [x] 2.1 Create `PasswordHasher` and `BCryptPasswordHasher` (fresh salt, check, legacy/non-hash values never match); verify `PasswordHasherTest`

## 3. Controller and wiring

- [x] 3.1 Inject the hasher into `UserController` (create and PASSWORD update hash after E1 validation; authenticate with `matches`) and wire it in `App`; update tests that inspected plaintext passwords; verify `mvn clean test`
- [x] 3.2 Add `PasswordProtectionTest`: file never contains the plaintext, delete/update with right/wrong password, password change persists a new hash that authenticates after restart, E1 rule still rejects `Clave12`, legacy plaintext record never authenticates

## 4. UML and verification

- [x] 4.1 Update `uml/model.puml`; verify `plantuml -checkonly`, `java -jar` of the shaded JAR runs a user session, and the regression script output is unchanged
