# Tasks

## 1. Maven project and executable JAR (F0-C01, F0-C02)

- [x] 1.1 Generate the project with `maven-archetype-quickstart:1.5` (groupId `es.upm.amusementpark`, artifactId `amusement-park`, package `es.upm.amusementpark`) at the repository root, set `maven.compiler.release` to 21, and verify `mvn -q package` succeeds
- [x] 1.2 Configure `finalName` `nombre-aplicacion` and `maven-jar-plugin` manifest `mainClass` `es.upm.amusementpark.App`; add `users.txt` and `target/` to `.gitignore`; verify `mvn package` produces `target/nombre-aplicacion.jar` and `java -jar target/nombre-aplicacion.jar` starts

## 2. User model (F1-C01)

- [x] 2.1 Create `model.User` with private attributes (first name, surnames, DNI final, email, password, phone, card), getters and setters for all except DNI; verify with `UserTest` (values retained, no DNI setter)
- [x] 2.2 Create `model.UserField` enum with `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; verify with a test asserting exactly these six values and no `DNI`

## 3. Validation (F1-C02)

- [x] 3.1 Implement `validation.UserValidator.isValidDni` (8 digits + control letter `TRWAGMYFPDXBNJZSQVHLCKE`) and verify `UserValidatorTest` accepts `12345678Z` and rejects `12345678A` and malformed shapes
- [x] 3.2 Implement `isValidEmail` using `split("@")`/`lastIndexOf('.')`, no regex; verify the test accepts `ana@correo.com`, `ana.nueva@correo.com` and rejects `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es`
- [x] 3.3 Implement `isValidPassword` with `Character` checks; verify the test accepts `Clave1!` and rejects `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12`
- [x] 3.4 Implement `isValidCard` (exactly 16 digits); verify the test accepts `1234567890123456` and rejects `1234 5678 9012 3456`, `123456789012345`, `12345678901234567`

## 4. Persistence (F1-C04)

- [x] 4.1 Implement `persistence.UserRepository` (`load()`/`saveAll()` on a configurable `Path`, default `users.txt`, `;`-separated lines, missing file = empty, malformed line skipped with warning); verify `UserRepositoryTest` round-trips users, handles a missing file and skips a malformed line, using a temp directory

## 5. User operations (F1-C03, F1-C05, F1-C06)

- [x] 5.1 Implement `service.UserService.create` (validation, DNI/email uniqueness, persist) with `UserOperationException`; verify `UserServiceTest` covers success, each invalid field, duplicate DNI, duplicate email, and that the file is updated
- [x] 5.2 Implement `UserService.delete(email, password)` (locate, verify existence, check password, delete, persist); verify tests for success, unknown email and wrong password (user kept)
- [x] 5.3 Implement `UserService.update(email, password, UserField, value)` with creation rules (email valid and not used by another user, password and card valid) and persistence; verify tests for each field, wrong credentials, invalid values, duplicate email, and that a reloaded repository sees the change

## 6. Command interface (F1-C03, F1-C07, F1-C08)

- [x] 6.1 Implement `command.CommandProcessor.process(line)` parsing with `split(";", -1)` and dispatching `USER CREATE`/`USER DELETE`/`USER UPDATE`/`HELP`/`EXIT`, printing `USER CREATED`/`USER DELETED`/`USER UPDATED`/`ERROR: …`, never throwing; verify `CommandProcessorTest` covers each command, wrong field counts, unknown command, unknown/`DNI` field, and HELP content
- [x] 6.2 Implement `command.CommandRunner.run(BufferedReader)` (trim, skip blank lines, stop after `EXIT` or EOF); verify `CommandRunnerTest` shows in-order processing, blank lines ignored and commands after `EXIT` not executed
- [x] 6.3 Implement `App.main`: no args → stdin, first arg → file reader through the same runner, missing/unreadable file → `ERROR:` message and controlled exit without stack trace; verify `AppTest` for file mode and missing-file handling

## 7. Delivery 1 integration (F1-C09)

- [x] 7.1 Run `mvn clean package` from scratch and verify all tests pass and `target/nombre-aplicacion.jar` is produced
- [x] 7.2 Run the JAR in file mode with the assignment's `input.txt` example (create Ana and Pedro, update phone and email, delete Pedro, HELP, EXIT) and in interactive mode via piped stdin; verify expected outputs and resulting `users.txt`, then remove the generated `users.txt`
- [x] 7.3 Verify the main sources contain no `java.util.regex`, `matches(`, `replaceAll(`, `replaceFirst(` and that `pom.xml` has no non-test dependencies
