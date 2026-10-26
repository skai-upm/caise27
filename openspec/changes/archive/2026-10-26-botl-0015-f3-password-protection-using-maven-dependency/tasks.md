# Tasks

## 1. Build

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin to `pom.xml`; verify `mvn package` produces `target/parque.jar` that contains `org/mindrot/jbcrypt/BCrypt.class` and runs with `java -jar`

## 2. Password hashing

- [x] 2.1 Create `PasswordHasher` (hash, matches, isHash; configurable cost); verify with unit tests
- [x] 2.2 Use it in `UserController` (create, PASSWORD update, authenticate, plaintext migration on configure) and inject it from `App`; update tests to inject a cost-4 hasher; verify unit tests for the four spec scenarios and that the users file holds no plaintext password
- [x] 2.3 Run `mvn test` and the regression script with the jar; verify all tests pass and the output is unchanged
- [x] 2.4 Update `uml/model.puml` with `PasswordHasher`; verify with `plantuml -checkonly`
