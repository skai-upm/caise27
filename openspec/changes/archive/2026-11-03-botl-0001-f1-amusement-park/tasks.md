# Tasks

## 1. F0-C01 — Project initialization

- [x] 1.1 Create `pom.xml` (groupId `es.upm.etsisi`, artifactId `amusement-park`, `maven.compiler.release` 21, JUnit 5 test scope, Quickstart plugin set) and the standard `src/main/java` / `src/test/java` layout; verify `mvn test` compiles and runs a baseline test

## 2. F0-C02 — Executable application

- [x] 2.1 Configure `<finalName>nombre-aplicacion</finalName>` and the `maven-jar-plugin` manifest `Main-Class` for `es.upm.etsisi.amusementpark.App`; verify `mvn package` produces `target/nombre-aplicacion.jar` and `java -jar target/nombre-aplicacion.jar` starts reading standard input

## 3. F1-C01 — User model

- [x] 3.1 Implement `User` with private attributes (first name, surnames, final DNI, email, password, phone, card), getters, and setters for every field except DNI; verify with `UserTest` that all attributes are readable and no DNI mutator exists

## 4. F1-C02 — User data validation

- [x] 4.1 Implement `UserValidator` (DNI with check-letter algorithm, email `text@text.xxx`, password character classes, 16-digit card) using only String/Character operations and `split`; verify `UserValidatorTest` covers every valid and invalid source example

## 5. F1-C03 — User creation

- [x] 5.1 Implement `UserException`, `UserService.create` (validation, DNI/email uniqueness, all checks before any mutation) and `CommandProcessor` parsing with `split(";", -1)` for `USER CREATE` and unknown commands; verify `UserServiceTest` and `CommandProcessorTest` cover success, duplicate DNI, duplicate email, invalid data, wrong field count and unknown command

## 6. F1-C04 — User persistence

- [x] 6.1 Implement `UserRepository` (load/save `;`-separated lines, missing file = empty, skip invalid lines with warning) and make `UserService` save a copy before replacing in-memory state; verify `UserRepositoryTest` covers round-trip, exact line format, missing file, malformed lines, and that a save failure leaves memory unchanged

## 7. F1-C05 — User deletion

- [x] 7.1 Implement `UserService.delete` and `USER DELETE` dispatch; verify tests cover deletion with correct credentials (memory and file), wrong password and unknown email leaving state unchanged

## 8. F1-C06 — User update

- [x] 8.1 Implement `UserField` enum and `UserService.update` plus `USER UPDATE` dispatch, revalidating EMAIL/PASSWORD/CARD and email uniqueness, rejecting DNI and unknown fields; verify tests cover each field, persistence across reload, and every rejection path leaving the user unchanged

## 9. F1-C07 — Command file input

- [x] 9.1 Implement `CommandProcessor.run(BufferedReader)` shared by both modes and `App` selecting standard input or `args[0]`, ignoring blank lines and reporting nonexistent/unreadable files without a stack trace; verify tests show identical results for the same sequence from a reader and from a file, blank lines ignored, and controlled missing-file handling

## 10. F1-C08 — Help and exit

- [x] 10.1 Implement `HELP` (lists the five commands with syntax) and `EXIT` (stops processing, later lines skipped); verify tests for help content and for a file where a `USER CREATE` after `EXIT` is not executed

## 11. F1-C09 — Delivery 1 integration

- [x] 11.1 Add an end-to-end test running the source `input.txt` example through `App` in file mode and a mixed valid/invalid sequence; verify outcomes and persisted file content
- [x] 11.2 Run `mvn clean package`, then run the JAR interactively and with a command file from a temporary directory; verify outputs and that production code uses no regex, encryption or runtime dependencies
