# Tasks

## 1. Maven project and executable JAR (F0-C01, F0-C02)

- [x] 1.1 Create `pom.xml` (quickstart layout, `maven.compiler.release` 21, JUnit Jupiter test scope, `finalName` `nombre-aplicacion`, `Main-Class` manifest) and verify `mvn package` succeeds
- [x] 1.2 Add entry point class `es.upm.etsisi.amusementpark.App` and verify `java -jar target/nombre-aplicacion.jar` starts and exits on EOF

## 2. User model (F1-C01)

- [x] 2.1 Implement `model.User` with private attributes, final DNI (getter only) and `model.UserField` enum; verify with `UserTest` that DNI has no setter and fields are read back

## 3. Validation (F1-C02)

- [x] 3.1 Implement `validation.UserValidator` (DNI, email, password, card) without regex; verify `UserValidatorTest` covers every valid/invalid example from the source

## 4. Persistence and repository (F1-C04)

- [x] 4.1 Implement `repository.UserFileStore` (load/save `;` lines, missing file = empty, malformed lines skipped) and `repository.UserRepository`; verify `UserRepositoryTest` round-trips users through a temp file and skips malformed lines

## 5. User operations (F1-C03, F1-C05, F1-C06)

- [x] 5.1 Implement `controller.UserController.create` with validation and DNI/email uniqueness; verify `UserControllerTest` success, invalid data and duplicate cases
- [x] 5.2 Implement `UserController.delete` (locate by email, check password, delete, persist); verify tests for success, wrong password and unknown email
- [x] 5.3 Implement `UserController.update` using `UserField` with validation and uniqueness, DNI immutable; verify tests for each field, invalid values, taken email, DNI/unknown field and wrong credentials

## 6. Command interface (F1-C03, F1-C07, F1-C08)

- [x] 6.1 Implement `cli.CommandProcessor` (split-based parsing, USER CREATE/DELETE/UPDATE, HELP, EXIT, ERROR messages, never throws) and verify `CommandProcessorTest` including unknown/malformed commands and HELP content
- [x] 6.2 Implement `cli.CommandRunner` shared by stdin and file modes (order, blank lines ignored, stop on EXIT) and wire `App` (file argument, missing file handled); verify `CommandRunnerTest` and `AppTest` (lines after EXIT not executed, missing file message)

## 7. Delivery 1 integration (F1-C09)

- [x] 7.1 Run `mvn clean package` from scratch and execute the JAR with the section 16 example file and interactively via piped stdin; verify outputs `USER CREATED`/`USER UPDATED`/`USER DELETED`, help text, and that `users.txt` persists across runs
