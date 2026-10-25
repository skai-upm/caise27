# Tasks

## 1. Build

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin (Main-Class `es.upm.parque.App`, final name `parque`) to `pom.xml`; verify `mvn package` produces `target/parque.jar` containing `org/mindrot/jbcrypt/BCrypt.class`

## 2. Hashing

- [x] 2.1 Create `PasswordHasher` and `BCryptPasswordHasher` in `business.security`; verify with `BCryptPasswordHasherTest` (hash/match, wrong password, isHash)
- [x] 2.2 Inject the hasher into `UserManager` (create, authenticate, password change, plaintext migration on load) and wire it in `Bootstrap` (cost 10) and `Park` (cost 4); update tests that asserted plaintext; verify `UserManagerTest` covers every password-security scenario and `mvn test` passes

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` (security classes, realisation, `UserManager` association, usage from `Bootstrap`); verify `plantuml -checkonly` and `UmlModelTest`
- [x] 3.2 Verify end to end with `java -jar target/parque.jar`: create/update/delete with passwords, plaintext absent from `data/users.txt`, legacy plaintext file migrated
