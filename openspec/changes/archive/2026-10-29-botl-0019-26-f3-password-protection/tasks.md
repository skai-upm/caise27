# Tasks

## 1. Build

- [x] 1.1 Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` (main JAR replaced, `Main-Class` kept); verify `mvn clean package` succeeds, `unzip -l target/amusement-park.jar` lists `org/mindrot/jbcrypt/BCrypt.class`, and `dependency:list -DincludeScope=runtime` shows only jbcrypt

## 2. Protection

- [x] 2.1 Add `business.security.PasswordProtector`, a protected `passwordHash` on `User`, and `ParkController.setPassword` / `verifyPassword`; verify with unit tests for hash ≠ plain, new value on change, correct/wrong password, user without password, unknown user
- [x] 2.2 Persist the hash in the users file (`email;dni;hash`, legacy `email;dni` accepted); verify with a restart test that the password still verifies and the file does not contain the plain password
- [x] 2.3 Update `uml/model.puml` and the build-rule test; run `mvn clean package` and verify `BUILD SUCCESS` and that the shaded JAR runs `HELP`
