# Tasks

## 1. F0-C01 — Project initialization

- [x] 1.1 Create `pom.xml` (Maven Quickstart layout, `maven.compiler.release` 21, JUnit Jupiter test scope) plus `src/main/java` and `src/test/java`; verify `mvn test` succeeds

## 2. F0-C02 — Executable application

- [x] 2.1 Add `App` entry point and configure `finalName` `nombre-aplicacion` and the `maven-jar-plugin` `Main-Class`; verify `mvn package` produces `target/nombre-aplicacion.jar` and `java -jar target/nombre-aplicacion.jar` starts and reads standard input

## 3. F1-C01 — User model

- [x] 3.1 Implement encapsulated `User` (seven attributes, final DNI with no setter, copy constructor) and verify with `UserTest` covering attribute access and DNI immutability

## 4. F1-C02 — User data validation

- [x] 4.1 Implement `UserValidator` (DNI check letter, email, password, card) without regular expressions; verify with `UserValidatorTest` covering every valid and invalid example from the spec

## 5. F1-C03 — User creation

- [x] 5.1 Implement `UserService.create` with validation and DNI/email uniqueness, and `CommandProcessor` parsing for `USER CREATE` and unknown commands; verify with tests for success, invalid data, malformed command, duplicate DNI, duplicate email and unknown command

## 6. F1-C04 — User persistence

- [x] 6.1 Implement `UserRepository` / `UserFileRepository` (one `;`-separated line per user, temp-file + move save, load on start-up, skip malformed lines) and wire saving into `UserService` with rollback on save failure; verify with tests for round-trip, missing file, restart reload and unchanged file on rejected operations

## 7. F1-C05 — User deletion

- [x] 7.1 Implement `UserService.delete` and `USER DELETE` parsing; verify with tests for successful deletion (memory and file), unknown email, wrong password and malformed command

## 8. F1-C06 — User update

- [x] 8.1 Implement `UserField` enum and `UserService.update` plus `USER UPDATE` parsing; verify with tests for each field, DNI/unknown field rejection, wrong credentials, duplicate email, invalid card and invalid password, and persistence of updates

## 9. F1-C07 — Command file input

- [x] 9.1 Implement `CommandRunner` shared by stdin and file mode (ordered, blank lines ignored) and controlled handling of nonexistent/unreadable files in `App`; verify with tests that run the same sequence in both modes and compare outputs and final state, and a test for a nonexistent file

## 10. F1-C08 — Help and exit

- [x] 10.1 Implement `HELP` (all five syntaxes) and `EXIT` (stops processing, later lines ignored); verify with tests for HELP output and a file containing a command after EXIT

## 11. F1-C09 — Delivery 1 integration

- [x] 11.1 Run the full suite and `mvn package`, then run the built JAR with the source's reference `input.txt` and with an interactive piped sequence including invalid commands; verify outputs and that only the expected users are stored in `users.txt`
- [x] 11.2 Verify Delivery 1 restrictions: no runtime dependencies in `pom.xml`, and no `java.util.regex`, `matches(`, `replaceAll(`, database or crypto usage in `src/main/java` (grep check)
