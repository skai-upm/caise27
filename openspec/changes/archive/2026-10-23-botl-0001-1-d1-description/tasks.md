# Tasks

## 1. Project initialization and executable JAR (F0-C01, F0-C02)

- [x] 1.1 Create `pom.xml` (quickstart-style coordinates `es.upm:amusement-park`, `maven.compiler.release` 21, UTF-8, JUnit Jupiter in `test` scope) and the standard `src/main/java` / `src/test/java` layout; verify `mvn -q compile` succeeds
- [x] 1.2 Configure `maven-jar-plugin` with `mainClass` `es.upm.amusementpark.App` and `finalName` `amusement-park`, add a minimal `App`; verify `mvn package` produces `target/amusement-park.jar` and `java -jar target/amusement-park.jar` starts

## 2. User model (F1-C01)

- [x] 2.1 Implement `model.User` with private fields, getters, setters only for modifiable attributes, and a `final` DNI, plus the `model.UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`) with case-insensitive parsing that rejects `DNI` and unknown names; verify with `UserTest` / `UserFieldTest`

## 3. Validation (F1-C02)

- [x] 3.1 Implement `validation.UserValidator` (DNI check letter, email, password, card, non-blank) without regex; verify `UserValidatorTest` covers every valid/invalid example from the user-management spec

## 4. User creation (F1-C03)

- [x] 4.1 Implement `persistence.UserRepository` and an in-memory stand-in for tests, then `service.UserService.create` with validation and DNI/email uniqueness (email case-insensitive); verify `UserServiceTest` covers success, invalid data, and duplicates
- [x] 4.2 Implement `command.CommandProcessor` with `;` parsing, keyword matching, argument-count checking, and `USER CREATE` producing `USER CREATED` / `ERROR: ...`; verify `CommandProcessorTest` covers creation, unknown commands, and wrong argument counts

## 5. User persistence (F1-C04)

- [x] 5.1 Implement `persistence.FileUserRepository` (one `;`-separated line per user, atomic temp-file write, missing file = empty, malformed/invalid/duplicate lines skipped with a warning) and make `UserService` commit in-memory changes only after a successful save; verify `FileUserRepositoryTest` (round trip, format, malformed line, missing file) and a failing-repository test in `UserServiceTest`

## 6. User deletion (F1-C05)

- [x] 6.1 Implement `UserService.delete` and `USER DELETE` (locate by email, check password, delete, persist); verify tests for success, wrong password, and unknown email, including that the file no longer holds the user

## 7. User update (F1-C06)

- [x] 7.1 Implement `UserService.update` and `USER UPDATE` for every `UserField`, reusing creation validation and email uniqueness (excluding the user itself) and re-keying on email change; verify tests for each field, invalid values, duplicate email, `DNI` rejection, wrong password, and persistence after reload

## 8. Command file input (F1-C07)

- [x] 8.1 Implement `command.CommandRunner` over a `BufferedReader` (blank lines skipped, same `CommandProcessor` for both modes) and wire `App` to use standard input with no args or the first argument as a file, with a controlled `ERROR` and non-zero exit for missing or unreadable files; verify `CommandRunnerTest` and an `AppTest` for the missing file

## 9. Help and exit (F1-C08)

- [x] 9.1 Add `HELP` (syntax of all five commands) and `EXIT` (stops the runner so later lines are not processed; end of input also stops cleanly); verify tests that `HELP` lists every command and that a `USER CREATE` after `EXIT` in a file is not executed

## 10. Delivery 1 integration (F1-C09)

- [x] 10.1 Document build and usage (commands, modes, `users.txt`) in `README.md`; verify the documented commands match the HELP output
- [x] 10.2 Run `mvn clean package` (all tests green) and, from a temporary directory, run the JAR with the assignment's example `input.txt` and interactively via piped stdin; verify the outputs `USER CREATED`/`UPDATED`/`DELETED`, the `users.txt` contents, and that a second run reloads the users
- [x] 10.3 Resolve the Sonar quality-gate failure from the first apply analysis (1 new violation: java:S3516 on `UserValidator.isValidPassword`) without changing behavior; verify all tests still pass and the SDDX apply analysis reports quality gate `OK`
