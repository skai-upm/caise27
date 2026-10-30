# Tasks

## 1. Maven project and executable JAR (F0-C01, F0-C02)

- [x] 1.1 Create `pom.xml` (groupId `es.upm.amusementpark`, artifactId `amusement-park`, `maven.compiler.release` 21, test-scoped JUnit 5, `finalName` `amusement-park`, jar manifest `mainClass` `es.upm.amusementpark.App`) and the standard `src/main/java` / `src/test/java` layout; verify `mvn -q package` succeeds
- [x] 1.2 Add `users.txt` to `.gitignore` and verify `git check-ignore users.txt` reports it

## 2. User model and validation (F1-C01, F1-C02)

- [x] 2.1 Implement encapsulated `User` with immutable DNI and the `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with `UserTest` (getters/setters, no DNI setter path via update)
- [x] 2.2 Implement `UserValidator` (DNI algorithm, email via String ops/split, password classes, 16-digit card) without regex; verify `UserValidatorTest` covers every valid/invalid example from the source

## 3. User operations and persistence (F1-C03 – F1-C06)

- [x] 3.1 Implement `UserRepository` (load at start-up, write-through save, `;`-separated one-line-per-user format, missing file = empty); verify `UserRepositoryTest` round-trips users via a temp file and checks the exact line format
- [x] 3.2 Implement `UserService.create` with validation and DNI/email uniqueness; verify `UserServiceTest` success, invalid-data and duplicate cases
- [x] 3.3 Implement `UserService.delete` (locate by email, existence, password check, delete, persist); verify `UserServiceTest` success, unknown-email and wrong-password cases and persistence after reload
- [x] 3.4 Implement `UserService.update` for each `UserField` with credential check, creation validation and email uniqueness, DNI immutable; verify `UserServiceTest` per-field success, wrong credentials, invalid value, duplicate email, persistence after reload

## 4. Command interface (F1-C03, F1-C07, F1-C08)

- [x] 4.1 Implement `CommandProcessor` (split on `;`, exact argument counts, blank values rejected, `USER CREATE/DELETE/UPDATE`, `HELP`, `EXIT`, unknown/malformed → `ERROR:`); verify `CommandProcessorTest` including HELP content and malformed lines
- [x] 4.2 Implement `CommandRunner` shared by both modes (skip blank lines, stop at EXIT, stop at end of input) and `App` entry point (no args → stdin, first arg → file, missing/unreadable file → `ERROR:` and controlled exit); verify `CommandRunnerTest` for ordering, blank lines, EXIT stopping later lines

## 5. Delivery 1 integration (F1-C09)

- [x] 5.1 Run `mvn clean package` from scratch and verify all tests pass and `target/amusement-park.jar` is produced
- [x] 5.2 Run the packaged JAR in file mode with the source's `input.txt` example and in interactive mode via piped stdin; verify the expected `USER CREATED/UPDATED/DELETED`, HELP output, EXIT behaviour, persistence across two runs, and a missing-file path handled without a stack trace
- [x] 5.3 Document build and usage (commands, modes, users file) in `README.md` and verify the documented commands run as written
