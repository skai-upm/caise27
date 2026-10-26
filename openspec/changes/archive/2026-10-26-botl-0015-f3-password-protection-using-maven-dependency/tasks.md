# Tasks

## 1. Dependency and build

- [x] 1.1 Declare `org.mindrot:jbcrypt:0.4` and configure `maven-shade-plugin` so `target/parque.jar` contains it; verify `mvn package` and that the JAR contains `org/mindrot/jbcrypt/BCrypt.class`

## 2. Password hashing

- [x] 2.1 Create `PasswordEncoder` and `BCryptPasswordEncoder` (cost configurable); verify unit tests for hash/matches/isHash and that the hash differs from the raw password
- [x] 2.2 Inject the encoder in `UserController.configure`: hash on create and password change after validation, verify with `matches` on delete/update, migrate legacy plaintext on load; wire it in `App`; verify tests for each scenario (wrong password rejected, new hash persisted, invalid password not hashed, legacy file rewritten)
- [x] 2.3 Update existing tests to inject a fast encoder and to assert hashes instead of plaintext; verify `mvn test` passes and an end-to-end run of the JAR leaves no plaintext password in `users.txt`

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with the security package; verify `plantuml -checkonly` and the model/code cross-check pass
