# Tasks

## 1. Maven project and executable JAR (F0-C01, F0-C02)

- [x] 1.1 Create `pom.xml` (quickstart layout, `maven.compiler.release=21`, JUnit Jupiter test scope, `finalName` `amusement-park`, jar manifest `mainClass` `es.upm.amusementpark.App`) and verify `mvn package` succeeds
- [x] 1.2 Create `App` entry point and verify `java -jar target/amusement-park.jar` starts and the manifest declares the main class

## 2. User model (F1-C01)

- [x] 2.1 Implement encapsulated `User` with final DNI and `UserField` enum; verify with `UserTest` (getters, setters, no DNI setter)

## 3. Validation (F1-C02)

- [x] 3.1 Implement `UserValidator` (DNI letter algorithm, email via String ops, password classes, 16-digit card) without regex; verify `UserValidatorTest` covers every valid/invalid example from the spec

## 4. Commands and user creation (F1-C03)

- [x] 4.1 Implement `UserService.create` with validation and DNI/email uniqueness; verify `UserServiceTest` creation cases (success, invalid data, duplicates, wrong field count)
- [x] 4.2 Implement `CommandProcessor` parsing/dispatch of `USER CREATE` with `USER CREATED`/`ERROR:` output and unknown-command handling; verify `CommandProcessorTest`

## 5. Persistence (F1-C04)

- [x] 5.1 Implement `UserRepository` load/save of `users.txt` (`;` lines, missing file = empty, malformed lines skipped); verify `UserRepositoryTest` round-trip and reload rejects duplicates

## 6. Deletion and modification (F1-C05, F1-C06)

- [x] 6.1 Implement `USER DELETE` (locate by email, check password, delete, persist); verify tests for success, wrong password, unknown email and file update
- [x] 6.2 Implement `USER UPDATE` for every `UserField` with validation and email uniqueness, DNI rejected; verify tests for each field, invalid values, wrong credentials, taken email and persistence

## 7. Input modes, HELP and EXIT (F1-C07, F1-C08)

- [x] 7.1 Implement `CommandRunner` shared by stdin and file modes (blank lines skipped, stop on EXIT) and controlled handling of missing/unreadable files in `App`; verify `CommandRunnerTest` and `AppTest` (missing file)
- [x] 7.2 Implement `HELP` text and `EXIT`; verify tests that HELP lists all five syntaxes and commands after EXIT are not executed

## 8. Delivery 1 integration (F1-C09)

- [x] 8.1 Run `mvn clean package`, then run the JAR with the section 16 `input.txt` example in a temp directory and interactively via piped stdin; verify output `USER CREATED`×2, `USER UPDATED`×2, `USER DELETED`, help text, and the persisted `users.txt`
