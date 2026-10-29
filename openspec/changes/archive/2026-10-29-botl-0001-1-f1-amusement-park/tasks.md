# Tasks

## 1. Maven project and executable JAR (F0-C01, F0-C02)

- [x] 1.1 Create `pom.xml` (quickstart layout, `maven.compiler.release` 21, JUnit 5 test scope, `finalName` amusement-park, jar manifest `mainClass`) and verify `mvn package` succeeds
- [x] 1.2 Add `App` entry point and verify `java -jar target/amusement-park.jar` starts and reads standard input
- [x] 1.3 Add `users.txt` to `.gitignore` and verify `git status` does not list it after a run

## 2. User model (F1-C01)

- [x] 2.1 Implement encapsulated `User` with immutable DNI and verify with `UserTest`
- [x] 2.2 Implement `UserField` enum (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) and verify it has exactly those values in a test

## 3. Validation (F1-C02)

- [x] 3.1 Implement `UserValidator` DNI, email, password and card checks without regex and verify every valid/invalid example from the source in `UserValidatorTest`

## 4. User operations and persistence (F1-C03 to F1-C06)

- [x] 4.1 Implement `UserRepository` load/save of the `;`-separated users file (missing file = empty, malformed lines skipped) and verify round-trip in `UserRepositoryTest`
- [x] 4.2 Implement `UserService.create` with validation and DNI/email uniqueness, persisting on success, and verify success, invalid data and duplicates in `UserServiceTest`
- [x] 4.3 Implement `UserService.delete` (locate by email, existence, password, delete, persist) and verify success, wrong password and unknown email in `UserServiceTest`
- [x] 4.4 Implement `UserService.update` with credential check, per-field validation, email uniqueness, DNI rejection and persistence, and verify each field and rejection case in `UserServiceTest`
- [x] 4.5 Verify persistence across restarts (new repository/service on same file sees create/update/delete) in `UserServiceTest`

## 5. Command processing (F1-C03, F1-C07, F1-C08)

- [x] 5.1 Implement `CommandProcessor` parsing with `split`, dispatch of USER CREATE/DELETE/UPDATE, argument-count checks and error capture, and verify in `CommandProcessorTest`
- [x] 5.2 Implement HELP and EXIT (no further lines after EXIT) and verify in `CommandProcessorTest`
- [x] 5.3 Implement the shared `run` loop used by interactive and file modes (blank lines ignored) and wire it in `App`, verifying a missing file is reported with a controlled exit in `AppTest`

## 6. Delivery 1 integration (F1-C09)

- [x] 6.1 Run `mvn clean package` from scratch and verify all tests pass and `target/amusement-park.jar` is produced
- [x] 6.2 Run the §16 example `input.txt` with `java -jar target/amusement-park.jar input.txt` in a temporary directory and verify outputs USER CREATED/UPDATED/DELETED, help text, and the resulting users file
- [x] 6.3 Pipe the same commands to `java -jar target/amusement-park.jar` (interactive mode) and verify identical output
