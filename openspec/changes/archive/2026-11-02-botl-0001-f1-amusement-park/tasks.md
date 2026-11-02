# Tasks

## 1. F0-C01 — Project initialization

- [x] 1.1 Create `pom.xml` (Quickstart layout, `es.upm.etsisi:amusement-park`, `maven.compiler.release=21`, JUnit 5 test scope, pinned surefire) with `src/main/java/es/upm/etsisi/park/App.java` and `src/test/java/es/upm/etsisi/park/AppTest.java`; verify `mvn test` passes
- [x] 1.2 Checkpoint F0-C01 (`[F0-C01] Initialize Maven project with JDK 21`): verify `mvn package` succeeds (milestone commit owned by the BOTL harness, see design.md)

## 2. F0-C02 — Executable application

- [x] 2.1 Configure `<finalName>nombre-aplicacion</finalName>` and the `maven-jar-plugin` manifest `mainClass`; verify `mvn package` produces `target/nombre-aplicacion.jar` and `java -jar target/nombre-aplicacion.jar` starts reading standard input
- [x] 2.2 Checkpoint F0-C02 (`[F0-C02] Configure executable JAR`): verify `java -jar` runs with no IDE and with a file argument

## 3. F1-C01 — User model

- [x] 3.1 Implement `User` with the seven encapsulated attributes, a `final` DNI with no setter, and package-private setters for the other fields; verify `UserTest` checks every attribute and confirms no DNI mutator exists
- [x] 3.2 Checkpoint F1-C01 (`[F1-C01] Add user model`): verify `mvn package` succeeds

## 4. F1-C02 — User data validation

- [x] 4.1 Implement `UserValidator` (DNI check letter, email `text@text.xxx`, password character classes, 16-digit card) without regex or libraries; verify `UserValidatorTest` covers every valid and invalid source example
- [x] 4.2 Checkpoint F1-C02 (`[F1-C02] Add user data validation`): verify `mvn package` succeeds and no `java.util.regex`/`matches(`/`replaceAll(` usage exists in `src/main`

## 5. F1-C03 — User creation

- [x] 5.1 Implement `UserException`, `UserService.create` (validation, blank-field check, DNI and email uniqueness) and `CommandProcessor` parsing `USER CREATE` with `split(";", -1)`, printing `USER CREATED` / `ERROR: ...` and rejecting unknown commands; verify `UserServiceTest` and `CommandProcessorTest` cover success, invalid data, duplicate DNI, duplicate email, malformed, and unknown command
- [x] 5.2 Checkpoint F1-C03 (`[F1-C03] Implement user creation`): verify `mvn package` succeeds

## 6. F1-C04 — User persistence

- [x] 6.1 Implement `UserRepository` (load from / atomically save to a `;`-separated file, skip malformed lines) and wire `UserService` to load on construction and save after each mutation, reverting memory if the save fails; verify `UserRepositoryTest` (round trip, exact line format, missing file, malformed line) and a service test showing the user survives a "restart" and rollback on save failure
- [x] 6.2 Checkpoint F1-C04 (`[F1-C04] Add user persistence`): verify `mvn package` succeeds and that `User` and `App` contain no file read or write logic

## 7. F1-C05 — User deletion

- [x] 7.1 Implement `UserService.delete(email, password)` and `USER DELETE` parsing, printing `USER DELETED`; verify tests for correct credentials (removed from memory and file), wrong password, unknown email, and malformed field count
- [x] 7.2 Checkpoint F1-C05 (`[F1-C05] Implement user deletion`): verify `mvn package` succeeds

## 8. F1-C06 — User update

- [x] 8.1 Implement the `UserField` enum and `UserService.update(email, password, field, value)` with authentication, per-field validation, email uniqueness against other users, blank rejection and persistence; add `USER UPDATE` parsing printing `USER UPDATED`; verify tests for each of the six fields, DNI and unknown field rejected, reused email, invalid card or password, wrong credentials, and persistence after restart
- [x] 8.2 Checkpoint F1-C06 (`[F1-C06] Implement user update`): verify `mvn package` succeeds

## 9. F1-C07 — Command file input

- [x] 9.1 Implement `CommandProcessor.run(BufferedReader, PrintStream)` used by both stdin and file mode (blank lines ignored, in order), and make `App` open the file from `args[0]` with controlled error reporting for missing or unreadable files; verify tests showing identical output and state for the same sequence from a file and from a stdin-like reader, plus missing-file handling with no exception
- [x] 9.2 Checkpoint F1-C07 (`[F1-C07] Add command file input`): verify `mvn package` and `java -jar target/nombre-aplicacion.jar <file>` process a sample file

## 10. F1-C08 — Help and exit

- [x] 10.1 Implement `HELP` (lists the syntax of all user and control commands) and `EXIT` (prints `BYE` and stops the loop); verify tests that HELP contains every required syntax line, and that a command after EXIT in a file is not executed
- [x] 10.2 Checkpoint F1-C08 (`[F1-C08] Add help and exit commands`): verify `mvn package` succeeds

## 11. F1-C09 — Delivery 1 integration

- [x] 11.1 Add an end-to-end test running the source §16 reference sequence (create Ana and Pedro, update phone and email, delete Pedro, HELP, EXIT) and a mixed valid/invalid sequence; verify the outputs and the final users file
- [x] 11.2 Verify Delivery 1 restrictions: `mvn dependency:list`-equivalent check shows no runtime dependencies, no regex usage in `src/main`, and a fresh `mvn clean package` yields a JAR that runs the reference `input.txt` via `java -jar` both in file mode and with piped stdin
- [x] 11.3 Checkpoint F1-C09 (`[F1-C09] Complete delivery 1 integration`): verify the full test suite passes
