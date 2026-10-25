# Tasks

## 1. Project setup (F0-C01, F0-C02)

- [x] 1.1 Create `pom.xml` (quickstart layout, `maven.compiler.release=21`, JUnit Jupiter test dependency, UTF-8) and the `src/main/java` / `src/test/java` tree under `es.upm.etsisi.park`; verify `mvn -q compile` succeeds
- [x] 1.2 Configure `maven-jar-plugin` with `Main-Class` `es.upm.etsisi.park.app.App` and `finalName` `amusement-park`; add `target/` and `users.txt` to `.gitignore`; verify `mvn package` produces `target/amusement-park.jar` and `java -jar` starts it

## 2. User model and validation (F1-C01, F1-C02)

- [x] 2.1 Implement `model.User` (encapsulated fields, final DNI) and `model.UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with `UserTest`
- [x] 2.2 Implement `validation.UserValidator` (DNI algorithm, email, password, card, non-blank) without regular expressions; verify `UserValidatorTest` covers every valid/invalid example from the user-validation spec

## 3. User operations and persistence (F1-C03 – F1-C06)

- [x] 3.1 Implement `persistence.UserRepository` and `UserFileRepository` (load on start, missing file = empty, malformed line skipped with warning, atomic rewrite); verify `UserFileRepositoryTest` round-trips users and checks the line format
- [x] 3.2 Implement `service.UserService` and `UserException` with create (validation + DNI/email uniqueness), delete (email + password) and update (password check, enum field, re-validation, email uniqueness, DNI immutable), persisting after each change; verify `UserServiceTest` covers all user-management scenarios

## 4. Command interface and execution modes (F1-C07, F1-C08)

- [x] 4.1 Implement `command.CommandProcessor` and `CommandResult` (split by `;`, case-insensitive keywords, argument-count checks, `USER CREATE/DELETE/UPDATE`, `HELP`, `EXIT`, unknown command, catch-all error rendering); verify `CommandProcessorTest`
- [x] 4.2 Implement `app.CommandRunner` (shared loop for any `BufferedReader`, ignores blank lines, stops at `EXIT`/EOF, prompt only in interactive mode) and `app.App` (no args = stdin, one arg = file, controlled error + non-zero exit on missing/unreadable file, `park.users.file` property); verify `CommandRunnerTest` covers ordering, blank lines and lines after `EXIT`
- [x] 4.3 Document build, run modes and commands in `README.md`; verify the documented commands run as written

## 5. Delivery 1 integration (F1-C09)

- [x] 5.1 Run `mvn package` from clean, then run the JAR with the example `input.txt` sequence from the assignment and with a missing file, and interactively with piped stdin; verify outputs (`USER CREATED`, `USER UPDATED`, `USER DELETED`, help text, controlled error) and that users persist across two runs
