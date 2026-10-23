# Tasks

## 1. Project setup

- [x] 1.1 Create `pom.xml` (groupId `es.upm.park`, artifactId `amusement-park`, Java 21 release, JUnit Jupiter test scope, executable JAR with `Main-Class` `es.upm.park.App` and finalName `amusement-park`) and the standard `src/main/java` / `src/test/java` layout; verify `mvn -q compile` succeeds
- [x] 1.2 Add `README.md` usage section (build, interactive mode, file mode, data file) and verify the documented commands match the pom configuration

## 2. User model and validation

- [x] 2.1 Implement `model.User` (encapsulated, immutable DNI) and `model.UserField` enum with case-insensitive `parse`; verify with `UserTest` / `UserFieldTest`
- [x] 2.2 Implement `validation.UserValidator` (DNI letter algorithm, email, password, card, non-empty text) without regular expressions; verify `UserValidatorTest` covers every valid/invalid example in the specs

## 3. Persistence

- [x] 3.1 Implement `persistence.UserFileRepository` (load with malformed-line skipping, missing file = empty, atomic rewrite on save); verify `UserFileRepositoryTest` round-trips users and handles missing/malformed files

## 4. User operations

- [x] 4.1 Implement `service.UserService.create` with validation and DNI/email uniqueness, persisting on success; verify `UserServiceTest` creation and duplicate scenarios
- [x] 4.2 Implement `UserService.delete` (locate by email, check password, persist); verify deletion, wrong-password and unknown-user tests
- [x] 4.3 Implement `UserService.update` for every `UserField` with re-validation, email uniqueness and DNI rejection; verify update tests including invalid values and wrong credentials

## 5. Command interface

- [x] 5.1 Implement `cli.CommandProcessor` (parsing with `split`, dispatch of USER CREATE/DELETE/UPDATE, HELP, EXIT, error handling for unknown commands and wrong field counts); verify `CommandProcessorTest`
- [x] 5.2 Implement `cli.CommandRunner` (blank-line skipping, stop on EXIT) and `App` (interactive vs file mode, controlled error for missing/unreadable file); verify `CommandRunnerTest` and `AppTest` (file mode with EXIT stopping later commands, missing file)

## 6. Integration

- [x] 6.1 Run `mvn package`, then run the JAR in file mode with the specification's example `input.txt` and interactively via piped stdin; verify outputs (`USER CREATED`, `USER UPDATED`, `USER DELETED`, help text) and that users persist across runs
