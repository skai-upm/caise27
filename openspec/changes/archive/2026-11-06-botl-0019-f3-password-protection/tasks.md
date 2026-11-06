# Tasks

## 1. Library and packaging

- [x] 1.1 Add the `org.mindrot:jbcrypt:0.4` dependency and the `maven-shade-plugin` (main class `es.upm.park.App`, final name unchanged), and verify that `mvn clean package` succeeds, `unzip -l target/amusement-park.jar` lists `org/mindrot/jbcrypt/BCrypt.class`, and `java -jar target/amusement-park.jar` still runs

## 2. Password component

- [x] 2.1 Add `PasswordHasher`, `BCryptPasswordHasher`, `PasswordPolicy` (default not-blank) and `PasswordService`, and verify with `PasswordServiceTest` that a protected value is a `$2a$` hash without the raw password, that salting gives different values, that verification succeeds and fails as expected (wrong password, malformed hash), and that blank or policy-violating passwords are rejected
- [x] 2.2 Wire `PasswordService` in `AppConfiguration`, and verify with `AppConfigurationTest` that a configured service protects and verifies

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` (security package) and `README.md` (dependency, password handling), and verify with the PlantUML syntax check and `mvn clean package`
