# Tasks

## 1. Build

- [x] 1.1 Add the `org.mindrot:jbcrypt:0.4` dependency and configure `maven-shade-plugin`; verify `mvn -q package` produces `target/amusement-park.jar` containing `org/mindrot/jbcrypt/BCrypt.class` and that it starts with `java -jar`

## 2. Password protection

- [x] 2.1 Add `PasswordHasher` and `BCryptPasswordHasher`, plus injection and `protectPassword`/`verifyPassword` in `ParkController`, wired in `App`; verify unit tests for hash format, random salt, correct and wrong password, an invalid stored value and a password change

## 3. Restrictions and documentation

- [x] 3.1 Update `TechnicalRestrictionsTest` (jBCrypt is the only runtime dependency, used only by the hasher) and `uml/model.puml`; run `mvn -q package` and verify all tests pass
