# Tasks

Stages are cumulative: each group must keep `mvn test` green and preserve the behavior of earlier groups.

## 1. Stage 1 — Bootstrap (Maven Quickstart, JDK 21)

- [x] 1.1 Create `pom.xml` (groupId `es.upm.amusementpark`, artifactId `amusement-park`, `maven.compiler.release` 21, UTF-8, JUnit Jupiter test-scope, surefire plugin) and the Quickstart directories `src/main/java/es/upm/amusementpark` and `src/test/java/es/upm/amusementpark`; verify the files exist
- [x] 1.2 Add `App` with a `main` method (no domain functionality yet) and `AppTest` smoke test; verify `mvn test` passes
- [x] 1.3 Configure `maven-jar-plugin` manifest `mainClass` `es.upm.amusementpark.App`; verify `mvn package` produces a JAR that runs with `java -jar`

## 2. Stage 2 — User management and persistence

- [x] 2.1 Implement encapsulated `User` (final DNI, getters, setters only for editable fields) and `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with `UserTest` that DNI has no setter and fields round-trip
- [x] 2.2 Implement `UserValidator` (DNI checksum, email, password, card, non-blank) without regex; verify with `UserValidatorTest` covering every valid/invalid example from the specs
- [x] 2.3 Implement `UserRepository` (load/save `;`-separated file, missing file = empty list, malformed lines skipped); verify with `UserRepositoryTest` using a temp directory (save → load round-trip, missing file)
- [x] 2.4 Implement `UserService` create/delete/update with validation, uniqueness, credential checks and persistence after each success; verify with `UserServiceTest` covering valid create, invalid fields, duplicate DNI/email, authenticated/failed delete, field updates, email-uniqueness on update, invalid password/card update, and restart (new service on same file) restoring state
- [x] 2.5 Implement `CommandProcessor` parsing `USER CREATE/DELETE/UPDATE` with `String.split(";", -1)`, reporting `OK:`/`ERROR:` messages and rejecting unknown commands or wrong parameter counts without throwing; wire `App` to load users and process stdin lines; verify with `CommandProcessorTest` (valid commands, insufficient parameters, unknown field `DNI`) and `mvn test`

## 3. Stage 3 — Execution modes, HELP and EXIT

- [x] 3.1 Add `HELP` (lists the five commands with syntax) and `EXIT` (process returns false) to `CommandProcessor`; verify with tests asserting HELP output and EXIT return value
- [x] 3.2 Refactor `App` so stdin and file modes both feed a `BufferedReader` into one shared loop over `CommandProcessor` (blank lines skipped, stop on EXIT or end of input) and handle missing/unreadable files with a message and no stack trace; verify with `AppTest` running file mode on temp files (ordered processing, blank lines, commands after EXIT not executed, nonexistent file message)
- [x] 3.3 Verify mode equivalence: test that the same command sequence through the stdin path and the file path yields identical output and stored state
- [x] 3.4 Add a short README section documenting build (`mvn test`, `mvn package`), run modes, and commands; verify the documented `java -jar` file-mode command runs as written

## 4. Integration verification

- [x] 4.1 Run `mvn clean package` and execute the JAR in file mode with the spec example sequence and in interactive mode piping `HELP`/`EXIT`; verify outputs, the `users.txt` contents, and that a second run restores users (duplicate create rejected)
