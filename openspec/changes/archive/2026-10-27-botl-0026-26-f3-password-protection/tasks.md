# Tasks

## 1. Build

- [x] 1.1 Add the jBCrypt dependency and the shade plugin to `pom.xml`; verify `mvn package` produces `target/amusement-park.jar` containing `org/mindrot/jbcrypt/BCrypt.class` and the right `Main-Class`

## 2. Password protection

- [x] 2.1 Add `PasswordHasher` / `BCryptPasswordHasher`, inject it into `UserService`, hash on create and password update, verify with BCrypt on delete/update; verify with `UserServiceTest` (stored value is BCrypt and verifies, invalid password rejected before hashing, new hash on change, wrong password rejected, plain-text stored value never matches)
- [x] 2.2 Update tests that compared plain passwords, update `uml/model.puml`; verify with `AppTest`/`Delivery1IntegrationTest` that the users file holds a BCrypt value and not the password, and that verification works after restart; run `mvn package`
- [x] 2.3 Run the JAR from an empty directory to create and update a user and verify the stored line contains a BCrypt value
